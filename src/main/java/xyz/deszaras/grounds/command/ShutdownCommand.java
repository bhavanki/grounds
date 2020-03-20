package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

public class ShutdownCommand extends Command {

  public ShutdownCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    return true;
  }

  public static ShutdownCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs) {
    return new ShutdownCommand(actor, player);
  }

}
