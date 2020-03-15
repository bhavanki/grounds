package xyz.deszaras.grounds.command;

import xyz.deszaras.grounds.model.Player;

public class ExitCommand extends Command {

  public ExitCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    return true;
  }
}
