package xyz.deszaras.grounds.command.actor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.ServerCommand;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;
import xyz.deszaras.grounds.server.Shell;

/**
 * Boots an actor.<p>
 *
 * Arguments: username<br>
 * Checks: player is GOD, actor is not ROOT
 */
public class BootActorCommand extends ServerCommand<Boolean> {

  private final String username;

  public BootActorCommand(Actor actor, Player player, Server server,
                          String username) {
    super(actor, player, server);
    this.username = Objects.requireNonNull(username);
  }

  @Override
  public Boolean execute() throws CommandException {
    checkIfServer();
    ActorCommand.checkIfRoot(actor, username);
    ActorCommand.checkIfGod(player);

    Collection<Shell> actorShells = server.getOpenShells().get(new Actor(username));
    if (actorShells == null) {
      actorShells = Collections.emptySet();
    }
    return actorShells.stream()
        .map(shell -> {
          Player shellPlayer = shell.getPlayer();
          boolean terminated = shell.terminate();
          String messageText = terminated ?
              String.format("Terminated shell for actor %s playing as %s",
                            username, shellPlayer.getName()) :
              String.format("Failed to terminate shell for actor %s playing as %s",
                            username, shellPlayer.getName());
          actor.sendMessage(new Message(player, Message.Style.INFO, messageText));
          return terminated;
        })
        .reduce(true, Boolean::logicalAnd);
  }

  public static BootActorCommand newCommand(Actor actor, Player player,
                                            Server server, List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String username = commandArgs.get(0);
    return new BootActorCommand(actor, player, server, username);
  }
}
