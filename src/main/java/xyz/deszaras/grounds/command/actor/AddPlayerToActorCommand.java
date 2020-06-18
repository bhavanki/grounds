package xyz.deszaras.grounds.command.actor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Adds a player to an existing actor.<p>
 *
 * Arguments: username and player ID<br>
 * Checks: player is GOD, actor is not ROOT
 */
public class AddPlayerToActorCommand extends Command<Boolean> {

  private final String username;
  private final UUID playerId;

  public AddPlayerToActorCommand(Actor actor, Player player, String username,
                                 UUID playerId) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.playerId = Objects.requireNonNull(playerId);
  }

  @Override
  public Boolean execute() throws CommandException {
    ActorCommand.checkIfGod(player);
    ActorCommand.checkIfRoot(actor, username);

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.addPlayer(playerId));
    if (!result) {
      throw new CommandException("I could not find the actor named " + username);
    }

    return ActorCommand.saveActorDatabase();
  }

  public static AddPlayerToActorCommand newCommand(Actor actor, Player player,
                                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    try {
      UUID playerId = UUID.fromString(commandArgs.get(1));
      return new AddPlayerToActorCommand(actor, player, commandArgs.get(0), playerId);
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Player ID is not a UUID", e);
    }
  }
}
