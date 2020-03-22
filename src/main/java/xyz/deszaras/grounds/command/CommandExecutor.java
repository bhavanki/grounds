package xyz.deszaras.grounds.command;

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

  public static final CommandExecutor INSTANCE =
      new CommandExecutor(new CommandFactory());

  private final CommandFactory commandFactory;
  private final ExecutorService commandExecutorService;

  private CommandExecutor(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
    commandExecutorService = Executors.newSingleThreadExecutor();
  }

  /**
   * Submits a new command to be run.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param commandLine command line entered in the shell
   * @return future for the command result
   */
  public Future<CommandResult> submit(Actor actor, Player player, String commandLine) {
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
   * executed at all, the exception from the attempt is recorded.
   */
  public static class CommandResult {
    private final boolean success;
    private final CommandException commandException;
    private final Class<? extends Command> commandClass;

    /**
     * Creates a new result.
     *
     * @param success true if the command was successful
     * @param commandClass type of the executed command
     */
    public CommandResult(boolean success, Class<? extends Command> commandClass) {
      this.success = success;
      commandException = null;
      this.commandClass = commandClass;
    }

    /**
     * Creates a new result for a failed command execution.
     *
     * @param e exception thrown when attempting to run the command
     */
    public CommandResult(CommandException e) {
      success = false;
      commandException = e;
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
     * Gets the exception thrown when execution was attempted for
     * the command.
     *
     * @return command exception
     */
    public Optional<CommandException> getCommandException() {
      return Optional.ofNullable(commandException);
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
    private final String commandLine;

    /**
     * Creates a new callable.
     *
     * @param actor actor submitting the command
     * @param player player currently assumed by the actor
     * @param commandLine command line entered in the shell
     */
    private CommandCallable(Actor actor, Player player, String commandLine) {
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
      } catch (CommandException e) {
        return new CommandResult(e);
      }
    }
  }
}
