package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

@PermittedRoles(roles = {})
public class ShutdownCommand extends ServerCommand<Boolean> {

  public ShutdownCommand(Actor actor, Player player, Server server) {
    super(actor, player, server);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    checkIfServer();
    server.requestServerShutdown();
    return true;
  }

  public static ShutdownCommand newCommand(Actor actor, Player player, Server server,
                                           List<String> commandArgs) {
    return new ShutdownCommand(actor, player, server);
  }
}
