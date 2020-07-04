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
import xyz.deszaras.grounds.util.Argon2Utils;

/**
 * Adds a new actor.<p>
 *
 * Arguments: username and password
 */
@PermittedRoles(roles = { Role.THAUMATURGE })
public class AddActorCommand extends Command<Boolean> {

  private final String username;
  private final String password;

  public AddActorCommand(Actor actor, Player player, String username,
                         String password) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.password = Objects.requireNonNull(password);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    boolean result =
        ActorDatabase.INSTANCE.createActorRecord(username,
            Argon2Utils.hashPassword(password));
    if (!result) {
      throw new CommandException("An actor named " + username + " already exists");
    }

    return ActorCommand.saveActorDatabase();
  }

  public static AddActorCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new AddActorCommand(actor, player, commandArgs.get(0), commandArgs.get(1));
  }
}
