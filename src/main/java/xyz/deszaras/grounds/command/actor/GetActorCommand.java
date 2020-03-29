package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

/**
 * Gets an existing actor.<p>
 *
 * Arguments: username<br>
 * Checks: player is GOD
 */
public class GetActorCommand extends Command {

  private final String username;

  public GetActorCommand(Actor actor, Player player, String username) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
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

    Optional<ActorRecord> actorRecord =
        ActorDatabase.INSTANCE.getActorRecord(username);
    if (actorRecord.isPresent()) {
      actor.sendMessage(actorRecord.get().toString());
      return true;
    } else {
      actor.sendMessage("I could not find the actor named " + username);
      return false;
    }
  }

  public static GetActorCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new GetActorCommand(actor, player, commandArgs.get(0));
  }
}
