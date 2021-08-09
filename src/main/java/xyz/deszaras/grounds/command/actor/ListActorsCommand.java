package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

/**
 * Lists actors.<p>
 *
 * Arguments: none
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class ListActorsCommand extends Command<String> {

  public ListActorsCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() {
    Set<ActorRecord> actors = ActorDatabase.INSTANCE.getAllActorRecords();
    if (actors.isEmpty()) {
      return "There are no actors.";
    }

    return actors.stream()
        .map(ActorRecord::getUsername)
        .sorted()
        .collect(Collectors.joining("\n"));
  }

  public static ListActorsCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    return new ListActorsCommand(actor, player);
  }
}
