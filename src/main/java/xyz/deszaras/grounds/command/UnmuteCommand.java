package xyz.deszaras.grounds.command;

import java.util.List;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Removes a thing from the player's mute list.
 *
 * Arguments: thing to remove from the mute list
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class UnmuteCommand extends Command<String> {

  private final Thing mutee;

  public UnmuteCommand(Actor actor, Player player, Thing mutee) {
    super(actor, player);
    this.mutee = mutee;
  }

  @Override
  protected String executeImpl() throws CommandException {

    List<Thing> muteList = player.getMuteList();
    if (muteList.contains(mutee)) {
      muteList.remove(mutee);
      player.setMuteList(muteList);
    }

    return "Unmuted " + mutee.getName();
  }

  public static UnmuteCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    Thing mutee = CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0),
                                                           Thing.class, player);
    return new UnmuteCommand(actor, player, mutee);
  }
}
