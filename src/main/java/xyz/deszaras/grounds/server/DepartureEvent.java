package xyz.deszaras.grounds.server;

import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.model.Player;

/**
 * An event posted when a player deprts - specifically, when an actor leaves
 * a player either on logout or by switching to a different one.
 */
public class DepartureEvent extends Event<Void> {
  DepartureEvent(Player player) {
    super(player, null, null);
  }
}
