package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

public class ExitCommand extends Command<Boolean> {

  public ExitCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public Boolean execute() {
    return true;
  }

  public static ExitCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs) {
    return new ExitCommand(actor, player);
  }

}
