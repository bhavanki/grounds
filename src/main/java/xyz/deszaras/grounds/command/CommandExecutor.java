package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
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
    CommandCallable callable = new CommandCallable(actor, player, commandLine);
    return commandExecutorService.submit(callable);
  }

  /**
   * Shuts down this executor.
   */
  public void shutdown() {
    commandExecutorService.shutdown();
  }

  /**
   * The result of running a command. If a command could not be
   * built, the exception from the attempt is recorded.
   */
  public static class CommandResult {
    private final boolean success;
    private final CommandFactoryException commandFactoryException;
    private final Class<? extends Command> commandClass;

    /**
     * Creates a new result for a command that was executed.
     *
     * @param success true if the command was successful
     * @param commandClass type of the executed command
     */
    public CommandResult(boolean success, Class<? extends Command> commandClass) {
      this.success = success;
      commandFactoryException = null;
      this.commandClass = commandClass;
    }

    /**
     * Creates a new result for a command that could not be built.
     *
     * @param e exception thrown when attempting to build the command
     */
    public CommandResult(CommandFactoryException e) {
      success = false;
      commandFactoryException = e;
      this.commandClass = null;
    }

    /**
     * Checks if the command execution was successful or not.
     *
     * @return true if the command was successful
     */
    public boolean isSuccessful() {
      return success;
    }

    /**
     * Gets the exception thrown when building the command failed.
     *
     * @return command exception
     */
    public Optional<CommandFactoryException> getCommandFactoryException() {
      return Optional.ofNullable(commandFactoryException);
    }

    /**
     * Gets the type of the command executed.
     *
     * @return command type
     */
    public Optional<Class<? extends Command>> getCommandClass() {
      return Optional.ofNullable(commandClass);
    }
  }

  /**
   * This callable actually executes a command. Instances of this
   * class live on the queue within the command executor service.
   */
  private class CommandCallable implements Callable<CommandResult> {

    private final Actor actor;
    private final Player player;
    private final List<String> commandLine;

    /**
     * Creates a new callable.
     *
     * @param actor actor submitting the command
     * @param player player currently assumed by the actor
     * @param commandLine command line entered in the shell
     */
    private CommandCallable(Actor actor, Player player, List<String> commandLine) {
      this.actor = actor;
      this.player = player;
      this.commandLine = commandLine;
    }

    @Override
    public CommandResult call() {
      try {
        Command command =
            commandFactory.getCommand(actor, player, commandLine);
        return new CommandResult(command.execute(), command.getClass());
      } catch (CommandFactoryException e) {
        return new CommandResult(e);
      }
    }
  }
}
