package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

/**
 * This class is responsible for executing commands. It is a singleton,
 * to have all commands executed through a single controlling entity.
 * Internally, this class uses only a single thread for command execution,
 * thereby serializing changes to the state of the game.
 *
 * @see CommandFactory
 */
public class CommandExecutor {

  private static final Map<String, Class<? extends Command>> COMMANDS;
  private static final List<BiFunction<List<String>, Player, List<String>>> TRANSFORMS;

  static {
    COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("LOOK", LookCommand.class)
        .put("L", LookCommand.class)
        .put("INSPECT", InspectCommand.class)
        .put("TELEPORT", TeleportCommand.class)
        .put("TP", TeleportCommand.class)
        .put("MOVE", MoveCommand.class)
        .put("GO", MoveCommand.class)
        .put("G", MoveCommand.class)
        .put("HOME", HomeCommand.class)
        .put("WHISPER", WhisperCommand.class)
        .put("TELL", WhisperCommand.class)
        .put("SAY", SayCommand.class)
        .put("POSE", PoseCommand.class)
        .put("BUILD", BuildCommand.class)
        .put("GET_ATTR", GetAttrCommand.class)
        .put("SET_ATTR", SetAttrCommand.class)
        .put("REMOVE_ATTR", RemoveAttrCommand.class)
        .put("CHANGE_POLICY", ChangePolicyCommand.class)
        .put("DESTROY", DestroyCommand.class)
        .put("TAKE", TakeCommand.class)
        .put("GET", TakeCommand.class)
        .put("DROP", DropCommand.class)
        .put("INVENTORY", InventoryCommand.class)
        .put("I", InventoryCommand.class)
        .put("CLAIM", ClaimCommand.class)
        .put("ABANDON", AbandonCommand.class)
        .put("INIT", InitCommand.class)
        .put("LOAD", LoadCommand.class)
        .put("SAVE", SaveCommand.class)
        .put("RUN", RunCommand.class)
        .put("SWITCH_PLAYER", SwitchPlayerCommand.class)
        .put("EXIT", ExitCommand.class)
        .put("INDEX", IndexCommand.class)
        .put("GET_ID", GetIdCommand.class)
        .put("ROLE", RoleCommand.class)
        .put("ACTOR", ActorCommand.class)
        .put("WHO", WhoCommand.class)
        .put("PREFERENCE", PreferenceCommand.class)
        .put("PREF", PreferenceCommand.class)
        .put("SHUTDOWN", ShutdownCommand.class)
        .put("HELP", HelpCommand.class)
        .build();

    TRANSFORMS = ImmutableList.<BiFunction<List<String>, Player, List<String>>>builder()
        // Use ':' as an alias for a POSE command starting with the player's
        // name. Example: `:waves.` => `POSE Bob waves.`
        .add((line, player) -> {
            if (line.get(0).startsWith(":")) {
              return ImmutableList.<String>builder()
                  .add("POSE")
                  .add(player.getName())
                  .add(line.get(0).substring(1))
                  .addAll(line.subList(1, line.size()))
                  .build();
            }
            return line;
          })
        // Use '>' as an alias for a SAY command. Example: `>Hello.` =>
        // `SAY Hello.`
        .add((line, player) -> {
            if (line.get(0).startsWith(">")) {
              return ImmutableList.<String>builder()
                  .add("SAY")
                  .add(line.get(0).substring(1))
                  .addAll(line.subList(1, line.size()))
                  .build();
            }
            return line;
          })
        // Use 'OOC' (case-insensitive) as an alias for 'SAY _ooc_'.
        .add((line, player) -> {
            if (line.get(0).equalsIgnoreCase("OOC")) {
              return ImmutableList.<String>builder()
                  .add("SAY")
                  .add("_ooc_")
                  .addAll(line.subList(1, line.size()))
                  .build();
            }
            return line;
          })
        // Use '%' as an alias for a SAY _ooc_ command. Example:
        // `%Let's play!` => `SAY _ooc_ Let's play!`
        .add((line, player) -> {
            if (line.get(0).startsWith("%")) {
              return ImmutableList.<String>builder()
                  .add("SAY")
                  .add("_ooc_")
                  .add(line.get(0).substring(1))
                  .addAll(line.subList(1, line.size()))
                  .build();
            }
            return line;
          })
        .build();
  }

  private static CommandExecutor theExecutor = null;

  public static synchronized void create(Server server) {
    if (theExecutor != null) {
      throw new IllegalStateException("The command executor has already been created");
    }
    theExecutor = new CommandExecutor(new CommandFactory(TRANSFORMS, COMMANDS, server));
  }

  public static synchronized CommandExecutor getInstance() {
    if (theExecutor == null) {
      throw new IllegalStateException("The command executor has not yet been created");
    }
    return theExecutor;
  }

  private final CommandFactory commandFactory;
  private final ExecutorService commandExecutorService;

  @VisibleForTesting
  CommandExecutor(CommandFactory commandFactory) {
    this.commandFactory = Objects.requireNonNull(commandFactory);
    commandExecutorService =
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                          .setDaemon(false)
                                          .setNameFormat("grounds-command")
                                          .build());
  }

  /**
   * Gets this executor's command factory.
   *
   * @return command factory
   */
  public CommandFactory getCommandFactory() {
    return commandFactory;
  }

  /**
   * Submits a new command to be run.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param commandLine command line entered in the shell
   * @return future for the command result
   */
  public Future<CommandResult> submit(Actor actor, Player player,
                                      List<String> commandLine) {
    CommandCallable callable = new CommandCallable(actor, player, commandLine,
                                                   commandFactory);
    return commandExecutorService.submit(callable);
  }

  /**
   * Submits a new command to be run.
   *
   * @param command command to run
   * @return future for the command result
   */
  public Future<CommandResult> submit(Command command) {
    CommandCallable callable = new CommandCallable(command);
    return commandExecutorService.submit(callable);
  }

  /**
   * Shuts down this executor.
   */
  public void shutdown() {
    commandExecutorService.shutdown();
  }
}
