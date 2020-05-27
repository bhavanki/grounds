package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

public class ShutdownCommand extends ServerCommand<Boolean> {

  public ShutdownCommand(Actor actor, Player player, Server server) {
    super(actor, player, server);
  }

  @Override
  public Boolean execute() throws CommandException {
    checkIfServer();
    if (!player.equals(Player.GOD)) {
      throw new CommandException("Only GOD may shutdown the game");
    }
    server.requestServerShutdown();
    return true;
  }

  public static ShutdownCommand newCommand(Actor actor, Player player, Server server,
                                           List<String> commandArgs) {
    return new ShutdownCommand(actor, player, server);
  }

  public static String help() {
    return "SHUTDOWN\n\n" +
        "Exits the shell and also shuts down the server.";
  }
}
