package xyz.deszaras.grounds.command;

import xyz.deszaras.grounds.model.Player;

/**
 * Does nothing.
 */
public class NoOpCommand extends Command<Boolean> {

  public NoOpCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public Boolean execute() {
    return true;
  }
}
