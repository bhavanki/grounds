package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

public class ExitCommand extends Command {

  public ExitCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    return true;
  }

  public static ExitCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs) {
    return new ExitCommand(actor, player);
  }
}
