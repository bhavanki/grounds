package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;
import xyz.deszaras.grounds.util.AnsiUtils;

/**
 * Gets an existing actor.<p>
 *
 * Arguments: username
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class GetActorCommand extends Command<String> {

  private final String username;

  public GetActorCommand(Actor actor, Player player, String username) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
  }

  @Override
  protected String executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    Optional<ActorRecord> actorRecord =
        ActorDatabase.INSTANCE.getActorRecord(username);
    if (actorRecord.isPresent()) {
      return formatDetails(actorRecord.get());
    } else {
      throw new CommandException("I could not find the actor named " + username);
    }
  }

  private static String formatDetails(ActorRecord r) {
    StringBuilder b = new StringBuilder();
    b.append(AnsiUtils.color("Username:        ", Ansi.Color.CYAN, false))
        .append(r.getUsername()).append("\n");
    b.append(AnsiUtils.color("Players:\n", Ansi.Color.CYAN, false));
    for (UUID id : r.getPlayers()) {
      Optional<Player> p = Universe.getCurrent().getThing(id, Player.class);
      if (p.isPresent()) {
        b.append("  ").append(AnsiUtils.listing(p.get(), true)).append("\n");
      } else {
        b.append("  ??? [").append(id.toString()).append("]\n");
      }
    }
    b.append(AnsiUtils.color("Last IP:         ", Ansi.Color.CYAN, false))
        .append(r.getMostRecentIPAddress()).append("\n");
    b.append(AnsiUtils.color("Last login time: ", Ansi.Color.CYAN, false))
        .append(r.getLastLoginTime()).append("\n");
    if (r.getLockedUntil() == null) {
      b.append(AnsiUtils.color("Unlocked\n", Ansi.Color.GREEN, false));
    } else {
      b.append(AnsiUtils.color("Locked until:    ", Ansi.Color.RED, false))
          .append(r.getLockedUntil()).append("\n");
    }
    if (r.getPreferences().size() > 0) {
      b.append(AnsiUtils.color("Preferences:", Ansi.Color.CYAN, false));
      for (Map.Entry<String, String> e : r.getPreferences().entrySet()) {
        b.append("\n  ").append(e.getKey()).append(" = ").append(e.getValue())
            .append("\n");
      }
    }
    return b.toString();
  }

  public static GetActorCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    return new GetActorCommand(actor, player, commandArgs.get(0));
  }
}
