package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.concurrent.Callable;

import xyz.deszaras.grounds.model.Player;

/**
 * This callable actually executes a command. Instances of this
 * class live on the queue within the command executor service.
 * The callable returns a {@link CommandResult}.
 */
public class CommandCallable<R> implements Callable<CommandResult<R>> {

  private final Actor actor;
  private final Player player;
  private final List<String> commandLine;
  private final CommandExecutor commandExecutor;
  private final Command<R> command;

  /**
   * Creates a new callable. The command is constructed by a command
   * factory.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param commandLine command line entered in the shell
   * @param commandExecutor command executor
   */
  public CommandCallable(Actor actor, Player player, List<String> commandLine,
                         CommandExecutor commandExecutor) {
    this.actor = actor;
    this.player = player;
    this.commandLine = commandLine;
    this.command = null;

    this.commandExecutor = commandExecutor;
  }

  /**
   * Creates a new callable. The command is already constructed.
   *
   * @param command command to execute
   * @param commandExecutor command executor
   */
  public CommandCallable(Command<R> command, CommandExecutor commandExecutor) {
    this.command = command;
    this.actor = null;
    this.player = null;
    this.commandLine = null;

    this.commandExecutor = commandExecutor;
  }

  @Override
  public CommandResult<R> call() {
    Command<R> commandToExecute;
    if (command != null) {
      // Command is pre-built.
      commandToExecute = command;
    } else {
      // Create the command using the factory.
      try {
        commandToExecute = commandExecutor.getCommandFactory()
            .getCommand(actor, player, commandLine);
      } catch (CommandFactoryException e) {
        return new CommandResult(e);
      }
    }

    // Execute the command!
    CommandResult<R> commandResult;
    try {
      commandResult = new CommandResult<>(commandToExecute.execute(),
                                          commandToExecute);
    } catch (CommandException e) {
      return new CommandResult<>(e);
    }

    // Post the events from the executed command to the event bus.
    commandToExecute.getEvents().forEach(e -> {
      commandExecutor.getCommandEventBus().post(e);
    });

    return commandResult;
  }
}
