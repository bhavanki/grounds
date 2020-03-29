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
 * Sets an actor's password.<p>
 *
 * Arguments: username and password<br>
 * Checks: player is GOD, actor is not ROOT
 */
public class SetActorPasswordCommand extends Command {

  private final String username;
  private final String password;

  public SetActorPasswordCommand(Actor actor, Player player, String username,
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

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.setPassword(HashedPasswordAuthenticator.hashPassword(password)));
    if (!result) {
      actor.sendMessage("I could not find the actor named " + username);
      return false;
    }

    return ActorCommand.saveActorDatabase(actor);
  }

  public static SetActorPasswordCommand newCommand(Actor actor, Player player,
                                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new SetActorPasswordCommand(actor, player, commandArgs.get(0), commandArgs.get(1));
  }
}
