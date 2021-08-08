package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

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
      return actorRecord.get().toString();
    } else {
      throw new CommandException("I could not find the actor named " + username);
    }
  }

  public static GetActorCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    return new GetActorCommand(actor, player, commandArgs.get(0));
  }
}
