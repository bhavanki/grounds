package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
// import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.server.Server;
import xyz.deszaras.grounds.server.Shell;

/**
 * Shows a listing of all connected actors in the player's current universe.<p>
 *
 * Checks: player is wizard in universe (might relax later)
 */
public class WhoCommand extends ServerCommand<String> {

  public WhoCommand(Actor actor, Player player, Server server) {
    super(actor, player, server);
  }

  @Override
  public String execute() throws CommandException {
    checkIfServer();
    if (!Role.isWizard(player, player.getUniverse())) {
      throw new PermissionException("You are not a wizard in your universe, so you may not see who is on");
    }

    Map<Actor, Shell> openShells = server.getOpenShells();
    Set<Actor> sortedActors = openShells.keySet().stream()
        .sorted((a1, a2) -> a1.getUsername().compareTo(a2.getUsername()))
        .collect(Collectors.toSet());

    StringBuilder b = new StringBuilder();
    b.append(String.format("%12.12s %25.25s\n", "ACTOR", "PLAYER"));
    b.append(String.format("%12.12s %25.25s\n", "-----", "------"));
    for (Actor actor : sortedActors) {
      Shell actorShell = openShells.get(actor);
      Player actorPlayer = actorShell.getPlayer();
      String playerName = actorPlayer != null ? actorPlayer.getName() : "-";
      b.append(String.format("%12.12s %25.25s\n", actor.getUsername(), playerName));
    }
    return b.toString();
  }

  public static WhoCommand newCommand(Actor actor, Player player, Server server,
                                      List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    return new WhoCommand(actor, player, server);
  }
}
