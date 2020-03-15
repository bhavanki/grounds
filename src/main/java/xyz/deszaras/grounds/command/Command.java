package xyz.deszaras.grounds.command;

import java.util.Objects;
import xyz.deszaras.grounds.model.Player;

/**
 * A command to observe or make a change to a universe.
 */
public abstract class Command {

  protected final Actor actor;
  protected final Player player;

  public Command(Actor actor, Player player) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
  }

  public abstract boolean execute();
}
