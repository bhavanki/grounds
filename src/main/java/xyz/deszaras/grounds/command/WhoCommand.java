package xyz.deszaras.grounds.command;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.server.Server;
import xyz.deszaras.grounds.server.Shell;
import xyz.deszaras.grounds.util.TabularOutput;
import xyz.deszaras.grounds.util.TimeUtils;

/**
 * Shows a listing of all connected players.<p>
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class WhoCommand extends ServerCommand<String> {

  public WhoCommand(Actor actor, Player player, Server server) {
    super(actor, player, server);
  }

  @Override
  protected String executeImpl() throws CommandException {
    checkIfServer();

    Map<Actor, Collection<Shell>> openShells = server.getOpenShells();
    List<Shell> sortedConnectedShells = openShells.values().stream()
        .flatMap(Collection::stream)
        .filter(s -> s.getPlayer().isPresent())
        .sorted((s1, s2) -> s1.getPlayer().get().getName().compareTo(s2.getPlayer().get().getName()))
        .collect(Collectors.toList());

    boolean isWizard = Role.isWizard(player);
    TabularOutput table = new TabularOutput();
    if (isWizard) {
      table.defineColumn("PLAYER", "%-16.16s")
          .defineColumn("ACTOR", "%-12.12s")
          .defineColumn("LOCATION", "%-16.16s")
          .defineColumn("DOING", "%-20.20s")
          .defineColumn("CONNECTED", "%s");
    } else {
      table.defineColumn("PLAYER", "%-16.16s")
          .defineColumn("DOING", "%-20.20s")
          .defineColumn("CONNECTED", "%s");
    }

    Instant now = Instant.now();
    for (Shell shell : sortedConnectedShells) {
      Player shellPlayer = shell.getPlayer().get();
      Duration connectionDuration = Duration.between(shell.getStartTime(), now);
      if (isWizard) {
        Actor shellActor = shell.getActor();
        Optional<Thing> shellPlayerLocation;
        try {
          shellPlayerLocation = shellPlayer.getLocation();
        } catch (MissingThingException e) {
          shellPlayerLocation = Optional.empty();
        }
        table.addRow(shellPlayer.getName(),
                     shellActor.getUsername(),
                     shellPlayerLocation.isPresent() ?
                         shellPlayerLocation.get().getName() : "<none>",
                     shellPlayer.getDoing().orElse(""),
                     TimeUtils.toString(connectionDuration));
      } else {
        table.addRow(shellPlayer.getName(),
                     shellPlayer.getDoing().orElse(""),
                     TimeUtils.toString(connectionDuration));
      }
    }
    return table.toString();
  }

  public static WhoCommand newCommand(Actor actor, Player player, Server server,
                                      List<String> commandArgs)
      throws CommandFactoryException {
    return new WhoCommand(actor, player, server);
  }
}
