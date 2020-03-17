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

  protected Command(Actor actor, Player player) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
  }

  public abstract boolean execute();

  protected static void ensureMinArgs(List<String> l, int n) throws CommandException {
    if (l.size() < n) {
      throw new CommandException("Need at least " + n + " arguments, got " + l.size());
    }
  }
}
