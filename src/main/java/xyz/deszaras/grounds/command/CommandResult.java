package xyz.deszaras.grounds.command;

import java.util.Optional;

/**
 * The result of running a command. If a command could not be
 * built, the exception from the attempt is recorded.
 *
 * @param R type of command return value
 */
public class CommandResult<R> {
  private final R result;
  private final CommandException commandException;
  private final CommandFactoryException commandFactoryException;
  private final Class<? extends Command> commandClass;

  /**
   * Creates a new result for a command that was executed.
   *
   * @param result command result
   * @param commandClass type of the executed command
   */
  public CommandResult(R result, Class<? extends Command> commandClass) {
    this.result = result;
    commandException = null;
    commandFactoryException = null;
    this.commandClass = commandClass;
  }

  /**
   * Creates a new result for a command that failed during execution.
   *
   * @param e exception thrown when executing the command
   */
  public CommandResult(CommandException e) {
    result = null;
    commandException = e;
    commandFactoryException = null;
    this.commandClass = null;
  }

  /**
   * Creates a new result for a command that could not be built.
   *
   * @param e exception thrown when attempting to build the command
   */
  public CommandResult(CommandFactoryException e) {
    result = null;
    commandException = null;
    commandFactoryException = e;
    this.commandClass = null;
  }

  /**
   * Checks if the command execution was successful or not.
   *
   * @return true if the command was successful
   */
  public boolean isSuccessful() {
    return commandException == null && commandFactoryException == null;
  }

  /**
   * Gets the result value.
   *
   * @return result value
   */
  public R getResult() {
    return result;
  }

  /**
   * Gets the exception thrown when executing the command failed.
   *
   * @return command exception
   */
  public Optional<CommandException> getCommandException() {
    return Optional.ofNullable(commandException);
  }

  /**
   * Gets the exception thrown when building the command failed.
   *
   * @return command factory exception
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
