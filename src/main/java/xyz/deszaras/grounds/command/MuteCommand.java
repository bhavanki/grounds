package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Adds a thing to the player's mute list.
 *
 * Arguments: thing to add to the mute list (optional)
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
    if (mutee == null) {
      List<Thing> muteList = player.getMuteList();
      if (muteList.isEmpty()) {
        return "Your mute list is empty.";
      }
      return muteList.stream()
          .map(t -> t.getName())
          .sorted()
          .collect(Collectors.joining(", "));
    }
    if (Player.GOD.equals(mutee)) {
      throw new CommandException("You may not mute GOD");
    }

    if (player.mute(mutee)) {
      return "Muted " + mutee.getName();
    } else {
      return "Already muted " + mutee.getName();
    }
  }

  public static MuteCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    Thing mutee;
    if (commandArgs.size() > 0) {
      mutee = CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0),
                                                       Thing.class, player);
    } else {
      mutee = null;
    }
    return new MuteCommand(actor, player, mutee);
  }
}
