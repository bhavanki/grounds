package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
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

  private static final DateTimeFormatter LOGIN_TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
          .withZone(ZoneId.systemDefault());

  private final boolean all;

  public WhoCommand(Actor actor, Player player, Server server, boolean all) {
    super(actor, player, server);
    this.all = all;
  }

  @Override
  public String execute() throws CommandException {
    checkIfServer();
    checkIfWizard("You are not a wizard, so you may not see who is on");

    Map<Actor, Collection<Shell>> openShells = server.getOpenShells();
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
            Instant lastLoginTime = r.getLastLoginTime();
            if (lastLoginTime != null) {
              a.setLastLoginTime(lastLoginTime);
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
    b.append(String.format("%12.12s %20.20s %15s %s\n", "ACTOR", "PLAYER", "IP", "LOGIN TIME"));
    b.append(String.format("%12.12s %20.20s %15s %s\n", "-----", "------", "--", "----------"));
    for (Actor actor : sortedActors) {
      String username = actor.getUsername();

      Collection<Shell> actorShells = openShells.get(actor);
      if (actorShells != null && !actorShells.isEmpty()) {
        for (Shell actorShell : actorShells) {
          Player actorPlayer = actorShell.getPlayer();
          String playerName = actorPlayer != null ? actorPlayer.getName() : "-";
          String ipAddress = actorShell.getIPAddress();
          String loginTime = LOGIN_TIME_FORMATTER.format(actorShell.getStartTime());
          b.append(String.format("%12.12s %20.20s %15s %s\n", username, playerName,
                                 ipAddress, loginTime));
        }
      } else {
        InetAddress mostRecentIPAddress = actor.getMostRecentIPAddress();
        String ipAddress = mostRecentIPAddress != null ?
            InetAddresses.toAddrString(mostRecentIPAddress) : "-";
        String lastLogin = actor.getLastLoginTime() != null ?
            LOGIN_TIME_FORMATTER.format(actor.getLastLoginTime()) : "-";
        b.append(String.format("%12.12s %20.20s %15s %s\n", username, "-",
                               ipAddress, lastLogin));
      }
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
