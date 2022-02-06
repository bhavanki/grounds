package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.security.CommandExecutorPermission;
import xyz.deszaras.grounds.server.Server;

/**
 * This class is responsible for executing commands. It is a singleton,
 * to have all commands executed through a single controlling entity.
 * Internally, this class uses only a single thread for command execution,
 * thereby serializing changes to the state of the game.<p>
 *
 * When running with a security manager, many methods of this class are
 * guarded by {@link CommandExecutorPermission}.
 *
 * @see CommandFactory
 */
public class CommandExecutor {

  static final Map<String, Class<? extends Command>> COMMANDS;
  private static final List<BiFunction<List<String>, Player, List<String>>> TRANSFORMS;

  static {
    COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("LOOK", LookCommand.class)
        .put("L", LookCommand.class)
        .put("DESCRIBE", DescribeCommand.class)
        .put("INSPECT", InspectCommand.class)
        .put("TELEPORT", TeleportCommand.class)
        .put("TP", TeleportCommand.class)
        .put("YOINK", YoinkCommand.class)
        .put("MOVE", MoveCommand.class)
        .put("GO", MoveCommand.class)
        .put("G", MoveCommand.class)
        .put("HOME", HomeCommand.class)
        .put("WHISPER", WhisperCommand.class)
        .put("TELL", WhisperCommand.class)
        .put("SAY", SayCommand.class)
        .put("PAGE", PageCommand.class)
        .put("POSE", PoseCommand.class)
        .put("BUILD", BuildCommand.class)
        .put("GET_ATTR", GetAttrCommand.class)
        .put("GET_ATTR_NAMES", GetAttrNamesCommand.class)
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
        .put("CHANGE_PASSWORD", ChangePasswordCommand.class)
        .put("SWITCH_PLAYER", SwitchPlayerCommand.class)
        .put("EXIT", ExitCommand.class)
        .put("INDEX", IndexCommand.class)
        .put("GET_ID", GetIdCommand.class)
        .put("ROLE", RoleCommand.class)
        .put("ACTOR", ActorCommand.class)
        .put("MAIL", MailCommand.class)
        .put("COMBAT", CombatCommand.class)
        .put("C", CombatCommand.class)
        .put("WHO", WhoCommand.class)
        .put("PREFERENCE", PreferenceCommand.class)
        .put("PREF", PreferenceCommand.class)
        .put("MUTE", MuteCommand.class)
        .put("UNMUTE", UnmuteCommand.class)
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

  /**
   * Permission needed to call {@link #create(Server)}.
   */
  public static final CommandExecutorPermission CREATE_PERMISSION =
      new CommandExecutorPermission("create");
  /**
   * Permission needed to call {@link #getInstance()}.
   */
  public static final CommandExecutorPermission GET_INSTANCE_PERMISSION =
      new CommandExecutorPermission("getInstance");
  /**
   * Permission needed to call {@link #submit(Actor,Player,List)} or
   * {@link #submit(Command)}.
   */
  public static final CommandExecutorPermission SUBMIT_PERMISSION =
      new CommandExecutorPermission("submit");
  /**
   * Permission needed to call {@link #shutdown()}.
   */
  public static final CommandExecutorPermission SHUTDOWN_PERMISSION =
      new CommandExecutorPermission("shutdown");

  private static CommandExecutor theExecutor = null;

  /**
   * Creates the single command executor for the game, if it doesn't already
   * exist.
   *
   * @param server server instance, if not in single-user mode
   * @throws IllegalStateException if the executor was already created
   */
  public static synchronized void create(Server server) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(CREATE_PERMISSION);
    }

    if (theExecutor != null) {
      throw new IllegalStateException("The command executor has already been created");
    }
    theExecutor = new CommandExecutor(new CommandFactory(TRANSFORMS, COMMANDS, server),
                                      new EventBus("commandEvents"));
  }

  /**
   * Gets the single command executor for the game.
   *
   * @return command executor
   * @throws IllegalStateException if the executor has not been created yet
   */
  public static synchronized CommandExecutor getInstance() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(GET_INSTANCE_PERMISSION);
    }

    if (theExecutor == null) {
      throw new IllegalStateException("The command executor has not yet been created");
    }
    return theExecutor;
  }

  private final CommandFactory commandFactory;
  private final ExecutorService commandExecutorService;
  private final EventBus commandEventBus;

  @VisibleForTesting
  CommandExecutor(CommandFactory commandFactory, EventBus commandEventBus) {
    this.commandFactory = Objects.requireNonNull(commandFactory);
    commandExecutorService =
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                          .setDaemon(false)
                                          .setNameFormat("grounds-command")
                                          .build());
    this.commandEventBus = commandEventBus;
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
   * Gets this executor's command event bus.
   *
   * @return command event bus
   */
  public EventBus getCommandEventBus() {
    return commandEventBus;
  }

  /**
   * Submits a new command to be run.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param commandLine command line entered in the shell
   * @return future for the command result
   */
  public Future<CommandResult> submit(Actor actor, Player player, List<String> commandLine) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(SUBMIT_PERMISSION);
    }

    try {
      return submit(commandFactory.getCommand(actor, player, commandLine));
    } catch (CommandFactoryException e) {
      return Futures.immediateFuture(new CommandResult(e));
    }
  }

  /**
   * Submits a new command to be run.
   *
   * @param command command to run
   * @return future for the command result
   */
  public <R> Future<CommandResult<R>> submit(Command<R> command) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(SUBMIT_PERMISSION);
    }

    CommandCallable callable = new CommandCallable(command, this);
    return commandExecutorService.submit(callable);
  }

  /**
   * Shuts down this executor.
   */
  public void shutdown() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(SHUTDOWN_PERMISSION);
    }

    commandExecutorService.shutdown();
  }
}
