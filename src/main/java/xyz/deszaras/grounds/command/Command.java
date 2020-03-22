package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;

/**
 * A command to observe or make a change to a universe.
 */
public abstract class Command {

  protected final Actor actor;
  protected final Player player;

  /**
   * Creates a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   */
  protected Command(Actor actor, Player player) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
  }

  /**
   * Executes the command.
   *
   * @return true if execution was successful
   */
  public abstract boolean execute();

  /**
   * Ensures that a list has a minimum length.
   *
   * @param l list
   * @param n minimum length
   * @throws CommandFactoryException if the list does not have enough elements
   */
  protected static void ensureMinArgs(List<String> l, int n) throws CommandFactoryException {
    if (l.size() < n) {
      throw new CommandFactoryException("Need at least " + n + " arguments, got " + l.size());
    }
  }
}
