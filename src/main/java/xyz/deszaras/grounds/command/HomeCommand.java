package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Sets the player's home, or teleports to home.
 *
 * Arguments: new home; omit to teleport to current home
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class HomeCommand extends Command<String> {

  private final Place newHome;

  public HomeCommand(Actor actor, Player player, Place newHome) {
    super(actor, player);
    this.newHome = newHome;
  }

  @Override
  protected String executeImpl() throws CommandException {
    if (newHome == null) {

      Place currentHome;
      try {
        Optional<Place> homeOpt = player.getHome();
        if (homeOpt.isEmpty()) {
          throw new CommandException("You have not set a home yet");
        }
        currentHome = homeOpt.get();
      } catch (MissingThingException e) {
        throw new CommandException("I cannot determine your current home location!");
      }

      return new TeleportCommand(actor, player, currentHome).executeImpl();
    }

    // TBD: Check if newHome may be set as home
    player.setHome(newHome);
    return "Home set";
  }

  public static HomeCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    Place newHome;
    if (commandArgs.size() > 0) {
      newHome = CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0),
                                                         Place.class, player);
    } else {
      newHome = null;
    }
    return new HomeCommand(actor, player, newHome);
  }
}
