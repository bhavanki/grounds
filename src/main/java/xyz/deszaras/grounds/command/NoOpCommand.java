package xyz.deszaras.grounds.command;

import xyz.deszaras.grounds.model.Player;

public class NoOpCommand extends Command {

  public NoOpCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    return true;
  }
}
