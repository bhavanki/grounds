package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

public abstract class ServerCommand<R> extends Command<R> {

  protected final Server server;

  protected ServerCommand(Actor actor, Player player, Server server) {
    super(actor, player);
    this.server = server;
  }

  @VisibleForTesting
  Server getServer() {
    return server;
  }

  protected void checkIfServer() throws CommandException {
    if (server == null) {
      throw new CommandException("The game is in single-user mode, server commands are unavailable");
    }
  }
}
