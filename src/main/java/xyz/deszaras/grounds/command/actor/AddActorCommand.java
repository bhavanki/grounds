package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.HashedPasswordAuthenticator;

/**
 * Adds a new actor.<p>
 *
 * Arguments: username and password<br>
 * Checks: player is GOD, actor is not ROOT
 */
public class AddActorCommand extends Command {

  private final String username;
  private final String password;

  public AddActorCommand(Actor actor, Player player, String username,
                         String password) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.password = Objects.requireNonNull(password);
  }

  @Override
  public boolean execute() {
    if (!player.equals(Player.GOD)) {
      actor.sendMessage("Only GOD may work with actors");
      return false;
    }
    if (!ActorCommand.checkIfRoot(actor, username)) {
      return false;
    }

    boolean result =
        ActorDatabase.INSTANCE.createActorRecord(username,
            HashedPasswordAuthenticator.hashPassword(password));
    if (!result) {
      actor.sendMessage("An actor named " + username + " already exists");
      return false;
    }

    return ActorCommand.saveActorDatabase(actor);
  }

  public static AddActorCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new AddActorCommand(actor, player, commandArgs.get(0), commandArgs.get(1));
  }
}
