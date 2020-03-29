package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Removes a player from an existing actor.<p>
 *
 * Arguments: username and player ID<br>
 * Checks: player is GOD, actor is not ROOT
 */
public class RemovePlayerFromActorCommand extends Command {

  private final String username;
  private final UUID playerId;

  public RemovePlayerFromActorCommand(Actor actor, Player player, String username,
                                 UUID playerId) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.playerId = Objects.requireNonNull(playerId);
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
        r -> r.removePlayer(playerId));
    if (!result) {
      actor.sendMessage("I could not find the actor named " + username);
      return false;
    }

    return ActorCommand.saveActorDatabase(actor);
  }

  public static RemovePlayerFromActorCommand newCommand(Actor actor, Player player,
                                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    try {
      UUID playerId = UUID.fromString(commandArgs.get(1));
      return new RemovePlayerFromActorCommand(actor, player, commandArgs.get(0), playerId);
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Player ID is not a UUID", e);
    }
  }
}
