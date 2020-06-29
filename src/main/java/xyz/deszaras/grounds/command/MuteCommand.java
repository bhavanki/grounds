package xyz.deszaras.grounds.command;

import java.util.List;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Adds a thing to the player's mute list.
 *
 * Arguments: thing to add to the mute list
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class MuteCommand extends Command<String> {

  private final Thing mutee;

  public MuteCommand(Actor actor, Player player, Thing mutee) {
    super(actor, player);
    this.mutee = mutee;
  }

  @Override
  protected String executeImpl() throws CommandException {
    if (Player.GOD.equals(mutee)) {
      throw new CommandException("You may not mute GOD");
    }

    List<Thing> muteList = player.getMuteList();
    if (!muteList.contains(mutee)) {
      muteList.add(mutee);
      player.setMuteList(muteList);
    }

    return "Muted " + mutee.getName();
  }

  public static MuteCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    Thing mutee = CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0),
                                                           Thing.class, player);
    return new MuteCommand(actor, player, mutee);
  }
}
