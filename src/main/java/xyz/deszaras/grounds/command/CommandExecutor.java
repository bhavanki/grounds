package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import xyz.deszaras.grounds.model.Player;

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
        .put("WHISPER", WhisperCommand.class)
        .put("TELL", WhisperCommand.class)
        .put("SAY", SayCommand.class)
        .put("POSE", PoseCommand.class)
        .put("BUILD", BuildCommand.class)
        .put("SET_ATTR", SetAttrCommand.class)
        .put("REMOVE_ATTR", RemoveAttrCommand.class)
        .put("TAKE", TakeCommand.class)
        .put("GET", TakeCommand.class)
        .put("DROP", DropCommand.class)
        .put("INVENTORY", InventoryCommand.class)
        .put("I", InventoryCommand.class)
        .put("CLAIM", ClaimCommand.class)
        .put("ABANDON", AbandonCommand.class)
        .put("LOAD", LoadCommand.class)
        .put("SAVE", SaveCommand.class)
        .put("SWITCH_PLAYER", SwitchPlayerCommand.class)
        .put("EXIT", ExitCommand.class)
        .put("HASH_PASSWORD", HashPasswordCommand.class)
        .put("INDEX", IndexCommand.class)
        .put("GET_ID", GetIdCommand.class)
        .put("ROLE", RoleCommand.class)
        .put("ACTOR", ActorCommand.class)
        .put("SHUTDOWN", ShutdownCommand.class)
        .put("HELP", HelpCommand.class)
        .build();
  }

  public static final CommandExecutor INSTANCE =
      new CommandExecutor(new CommandFactory(COMMANDS));

  private final CommandFactory commandFactory;
  private final ExecutorService commandExecutorService;

  private CommandExecutor(CommandFactory commandFactory) {
    this.commandFactory = Objects.requireNonNull(commandFactory);
    commandExecutorService = Executors.newSingleThreadExecutor();
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
   * Shuts down this executor.
   */
  public void shutdown() {
    commandExecutorService.shutdown();
  }
}
