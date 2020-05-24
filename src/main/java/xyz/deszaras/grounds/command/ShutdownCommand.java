package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

public class ShutdownCommand extends Command<Boolean> {

  public ShutdownCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.equals(Player.GOD)) {
      throw new CommandException("Only GOD may shutdown the game");
    }
    return true;
  }

  public static ShutdownCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs) {
    return new ShutdownCommand(actor, player);
  }

  public static String help() {
    return "SHUTDOWN\n\n" +
        "Exits the shell and also shuts down the server.";
  }
}
