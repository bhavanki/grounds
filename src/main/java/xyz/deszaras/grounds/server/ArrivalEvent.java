package xyz.deszaras.grounds.server;

import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.model.Player;

/**
 * An event posted when a player arrives - specifically, when an actor takes
 * up a player either on initial login or by switching to them.
 */
public class ArrivalEvent extends Event<Void> {
  ArrivalEvent(Player player) {
    super(player, null, null);
  }
}
