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

  /**
   * Creates a new callable.
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
  }

  @Override
  public CommandResult call() {
    try {
      // Create the command using the factory.
      Command command =
          commandFactory.getCommand(actor, player, commandLine);
      // Execute the command!
      return new CommandResult(command.execute(), command.getClass());
    } catch (CommandFactoryException e) {
      return new CommandResult(e);
    }
  }
}
