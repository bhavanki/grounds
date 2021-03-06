package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Removes an existing actor.<p>
 *
 * Arguments: username
 */
@PermittedRoles(roles = {})
public class RemoveActorCommand extends Command<Boolean> {

  private final String username;

  public RemoveActorCommand(Actor actor, Player player, String username) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    // TBD: Check if actor is connected.

    ActorDatabase.INSTANCE.removeActorRecord(username);

    return ActorCommand.saveActorDatabase();
  }

  public static RemoveActorCommand newCommand(Actor actor, Player player,
                                              List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new RemoveActorCommand(actor, player, commandArgs.get(0));
  }
}
