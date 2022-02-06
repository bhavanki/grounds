package xyz.deszaras.grounds.command;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * This callable actually executes a command. Instances of this
 * class live on the queue within the command executor service.
 * The callable returns a {@link CommandResult}.
 */
public class CommandCallable<R> implements Callable<CommandResult<R>> {

  private final Command<R> command;
  private final CommandExecutor commandExecutor;

  /**
   * Creates a new callable.
   *
   * @param command command to execute
   * @param commandExecutor command executor
   */
  public CommandCallable(Command<R> command, CommandExecutor commandExecutor) {
    this.command = Objects.requireNonNull(command);
    this.commandExecutor = Objects.requireNonNull(commandExecutor);
  }

  @Override
  public CommandResult<R> call() {
    // Execute the command!
    CommandResult<R> commandResult;
    try {
      commandResult = new CommandResult<>(command.execute(), command);
    } catch (CommandException e) {
      return new CommandResult<>(e);
    }

    // Post the events from the executed command to the event bus.
    command.getEvents().forEach(e -> {
      commandExecutor.getCommandEventBus().post(e);
    });

    return commandResult;
  }
}
