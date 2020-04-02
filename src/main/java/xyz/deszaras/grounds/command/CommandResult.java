package xyz.deszaras.grounds.command;

import java.util.Optional;

/**
 * The result of running a command. If a command could not be
 * built, the exception from the attempt is recorded.
 */
public class CommandResult {
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
