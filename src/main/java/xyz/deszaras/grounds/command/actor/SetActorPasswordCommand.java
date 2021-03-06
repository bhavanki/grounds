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
 * Sets an actor's password.<p>
 *
 * Arguments: username and password
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class SetActorPasswordCommand extends Command<Boolean> {

  private final String username;
  private final String password;

  public SetActorPasswordCommand(Actor actor, Player player, String username,
                                 String password) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.password = Objects.requireNonNull(password);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    if (Actor.ROOT.getUsername().equals(username)) {
      player.sendMessage(newInfoMessage("Setting password for root actor!"));
      // Create the root actor in the database (OK if already present).
      ActorDatabase.INSTANCE.createActorRecord(username,
          Argon2Utils.hashPassword(password));
      // The root actor may only play as GOD.
      ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.addPlayer(Player.GOD.getId()));
    }

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.setPassword(Argon2Utils.hashPassword(password)));
    if (!result) {
      throw new CommandException("I could not find the actor named " + username);
    }

    return ActorCommand.saveActorDatabase();
  }

  public static SetActorPasswordCommand newCommand(Actor actor, Player player,
                                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new SetActorPasswordCommand(actor, player, commandArgs.get(0), commandArgs.get(1));
  }
}
