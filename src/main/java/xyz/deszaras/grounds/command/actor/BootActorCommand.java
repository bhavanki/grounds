package xyz.deszaras.grounds.command.actor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.ServerCommand;
import xyz.deszaras.grounds.command.CommandArgumentResolver;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;
import xyz.deszaras.grounds.server.Shell;

/**
 * Boots an actor.<p>
 *
 * Arguments: username, optional player<br>
 * Checks: player is GOD, actor is not ROOT
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class BootActorCommand extends ServerCommand<Boolean> {

  private final String username;
  private final Player bootedPlayer;

  public BootActorCommand(Actor actor, Player player, Server server,
                          String username, Player bootedPlayer) {
    super(actor, player, server);
    this.username = Objects.requireNonNull(username);
    this.bootedPlayer = bootedPlayer;
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    checkIfServer();
    ActorCommand.checkIfRoot(player, username);

    Collection<Shell> actorShells = server.getOpenShells().get(new Actor(username));
    if (actorShells == null) {
      actorShells = Collections.emptySet();
    }
    return actorShells.stream()
        .filter(shell -> {
          if (bootedPlayer == null) {
            return true;
          }
          return shell.getPlayer().equals(Optional.of(bootedPlayer));
        })
        .map(shell -> {
          Optional<Player> shellPlayer = shell.getPlayer();
          String shellPlayerName = shellPlayer.isPresent() ?
              shellPlayer.get().getName() : "<no player>";
          boolean terminated = shell.terminate();
          String messageText = terminated ?
              String.format("Terminated shell for actor %s playing as %s",
                            username, shellPlayerName) :
              String.format("Failed to terminate shell for actor %s playing as %s",
                            username, shellPlayerName);
          player.sendMessage(new Message(player, Message.Style.INFO, messageText));
          return terminated;
        })
        .reduce(true, Boolean::logicalAnd);
  }

  public static BootActorCommand newCommand(Actor actor, Player player,
                                            Server server, List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String username = commandArgs.get(0);
    Player bootedPlayer = commandArgs.size() > 1 ?
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(1), Player.class, player) :
        null;
    return new BootActorCommand(actor, player, server, username, bootedPlayer);
  }
}
