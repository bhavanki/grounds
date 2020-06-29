package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Unlocks an actor, so that they can log in.<p>
 *
 * Arguments: username
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class UnlockActorCommand extends Command<Boolean> {

  private final String username;

  public UnlockActorCommand(Actor actor, Player player, String username) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.setLockedUntil(null));
    if (!result) {
      throw new CommandException("I could not find the actor named " + username);
    }

    return ActorCommand.saveActorDatabase();
  }

  public static UnlockActorCommand newCommand(Actor actor, Player player,
                                              List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String username = commandArgs.get(0);
    return new UnlockActorCommand(actor, player, username);
  }
}
