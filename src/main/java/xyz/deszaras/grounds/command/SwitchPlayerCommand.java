package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.util.UUIDUtils;

/**
 * Switches to a different player.
 *
 * Arguments: name or ID of new player
 * Checks: if the player is permitted for the actor (GOD and the root
 * actor may switch to any player)
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class SwitchPlayerCommand extends Command<Boolean> {

  private final Player newPlayer;

  public SwitchPlayerCommand(Actor actor, Player player, Player newPlayer) {
    super(actor, player);
    this.newPlayer = newPlayer;
  }

  public Player getNewPlayer() {
    return newPlayer;
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (!actor.equals(Actor.ROOT) &&
        !player.equals(Player.GOD) &&
        !ActorDatabase.INSTANCE.getActorRecord(actor.getUsername()).get()
        .getPlayers().contains(newPlayer.getId())) {
      throw new CommandException("You do not have permission to play as " +
                                 newPlayer.getName());
    }

    if (newPlayer.getCurrentActor().isPresent()) {
      throw new CommandException("Someone is already playing as " +
                                 newPlayer.getName());
    }

    player.setCurrentActor(null);
    newPlayer.setCurrentActor(actor);

    return true;
  }

  public static SwitchPlayerCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);

    String nameOrId = commandArgs.get(0);
    Optional<Player> newPlayer;
    if (UUIDUtils.isUUID(nameOrId)) {
      newPlayer = Universe.getCurrent().getThing(nameOrId, Player.class);
    } else {
      newPlayer = Universe.getCurrent().getThingByName(nameOrId, Player.class);
    }
    if (newPlayer.isEmpty()) {
      throw new CommandFactoryException("Player " + nameOrId + " not found");
    }
    return new SwitchPlayerCommand(actor, player, newPlayer.get());
  }
}
