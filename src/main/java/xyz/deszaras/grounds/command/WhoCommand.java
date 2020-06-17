package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.Server;
import xyz.deszaras.grounds.server.Shell;

/**
 * Shows a listing of all connected actors or all actors.<p>
 *
 * Arguments: "all" to list all actors, omit for just connected ones
 * Checks: player is wizard (might relax later)
 */
public class WhoCommand extends ServerCommand<String> {

  private final boolean all;

  public WhoCommand(Actor actor, Player player, Server server, boolean all) {
    super(actor, player, server);
    this.all = all;
  }

  @Override
  public String execute() throws CommandException {
    checkIfServer();
    checkIfWizard("You are not a wizard, so you may not see who is on");

    Map<Actor, Shell> openShells = server.getOpenShells();
    List<Actor> sortedConnectedActors = openShells.keySet().stream()
        .sorted((a1, a2) -> a1.getUsername().compareTo(a2.getUsername()))
        .collect(Collectors.toList());

    List<Actor> sortedActors;
    if (all) {
      List<Actor> sortedOtherActors =
          ActorDatabase.INSTANCE.getAllActorRecords().stream()
          .filter(r -> sortedConnectedActors.stream()
                      .noneMatch(a -> a.getUsername().equals(r.getUsername())))
          .map(r -> {
            Actor a = new Actor(r.getUsername());
            String ipAddressString = r.getMostRecentIPAddress();
            if (ipAddressString != null) {
              a.setMostRecentIPAddress(InetAddresses.forString(ipAddressString));
            }
            return a;
          })
          .sorted((a1, a2) -> a1.getUsername().compareTo(a2.getUsername()))
          .collect(Collectors.toList());
      sortedActors = ImmutableList.<Actor>builder()
          .addAll(sortedConnectedActors)
          .addAll(sortedOtherActors)
          .build();
    } else {
      sortedActors = sortedConnectedActors;
    }

    StringBuilder b = new StringBuilder();
    b.append(String.format("%12.12s %25.25s %s\n", "ACTOR", "PLAYER", "IP"));
    b.append(String.format("%12.12s %25.25s %s\n", "-----", "------", "--"));
    for (Actor actor : sortedActors) {
      Shell actorShell = openShells.get(actor);
      String playerName;
      String ipAddress;
      if (actorShell != null) {
        Player actorPlayer = actorShell.getPlayer();
        playerName = actorPlayer != null ? actorPlayer.getName() : "-";
        ipAddress = actorShell.getIPAddress();
      } else {
        playerName = "-";
        InetAddress mostRecentIPAddress = actor.getMostRecentIPAddress();
        ipAddress = mostRecentIPAddress != null ?
            InetAddresses.toAddrString(mostRecentIPAddress) : "-";
      }
      b.append(String.format("%12.12s %25.25s %s\n", actor.getUsername(), playerName,
                             ipAddress));
    }
    return b.toString();
  }

  public static WhoCommand newCommand(Actor actor, Player player, Server server,
                                      List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    boolean all = commandArgs.size() > 0 &&
        commandArgs.get(0).equalsIgnoreCase("ALL");
    return new WhoCommand(actor, player, server, all);
  }
}
