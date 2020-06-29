package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

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
    actor.setCurrentPlayer(newPlayer);
    newPlayer.setCurrentActor(actor);

    return true;
  }

  public static SwitchPlayerCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Player newPlayer =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    return new SwitchPlayerCommand(actor, player, newPlayer);
  }
}
