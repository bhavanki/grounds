package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.concurrent.Callable;
import xyz.deszaras.grounds.model.Player;

/**
 * This callable actually executes a command. Instances of this
 * class live on the queue within the command executor service.
 * The callable returns a {@link CommandResult}.
 */
public class CommandCallable implements Callable<CommandResult> {

  private final Actor actor;
  private final Player player;
  private final List<String> commandLine;
  private final CommandFactory commandFactory;
  private final Command command;

  /**
   * Creates a new callable. The command is constructed by a command
   * factory.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param commandLine command line entered in the shell
   * @param commandFactory command factory
   */
  public CommandCallable(Actor actor, Player player, List<String> commandLine,
                         CommandFactory commandFactory) {
    this.actor = actor;
    this.player = player;
    this.commandLine = commandLine;
    this.commandFactory = commandFactory;

    this.command = null;
  }

  /**
   * Creates a new callable. The command is already constructed.
   *
   * @param command command to execute
   */
  public CommandCallable(Command command) {
    this.command = command;

    this.actor = null;
    this.player = null;
    this.commandLine = null;
    this.commandFactory = null;
  }

  @Override
  public CommandResult call() {
    Command commandToExecute;
    if (command != null) {
      // Command is pre-built.
      commandToExecute = command;
    } else {
      // Create the command using the factory.
      try {
        commandToExecute = commandFactory.getCommand(actor, player, commandLine);
      } catch (CommandFactoryException e) {
        return new CommandResult(e);
      }
    }

    // Execute the command!
    try {
      return new CommandResult(commandToExecute.execute(),
                               commandToExecute);
    } catch (CommandException e) {
      return new CommandResult(e);
    }
  }
}
