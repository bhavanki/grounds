package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Moves a player to a destination through a link. Delegates the
 * actual transition to {@link TeleportCommand}.<p>
 *
 * Arguments: exit name<br>
 * Checks: player has a location; link at location has exit name;
 * place on other end of link exists; player passes USE of link
 */
public class MoveCommand extends Command<String> {

  private final String exitName;

  public MoveCommand(Actor actor, Player player, String exitName) {
    super(actor, player);
    this.exitName = Objects.requireNonNull(exitName);
  }

  @Override
  public String execute() throws CommandException {
    Optional<Place> source = player.getLocation();
    if (!source.isPresent()) {
      throw new CommandException("You have no current location, so you cannot move elsewhere");
    }

    // Check that there is a link associated with the player's current
    // location whose other place name matches the exit name. That other
    // place is the move destination.
    Optional<Place> moveDestination = null;
    Optional<Link> viaLink = null;
    for (Link link : Universe.getCurrent().findLinks(source.get())) {
      Optional<Attr> otherPlace = link.getOtherPlace(source.get());
      if (otherPlace.isPresent() &&
          otherPlace.get().getName().equalsIgnoreCase(exitName)) {
        moveDestination =
            Universe.getCurrent().getThing(otherPlace.get().getThingValue(),
                                           Place.class);
        viaLink = Optional.of(link);
      }
    }
    if (moveDestination == null) {
      throw new CommandException("I can't see an exit named " + exitName);
    }
    if (!moveDestination.isPresent()) {
      throw new CommandException("The exit has another side, but I can't find that place!");
    }
    checkPermission(Category.USE, viaLink.get(),
                    "You are not permitted to traverse the exit to that place");

    return new TeleportCommand(actor, player, moveDestination.get()).execute();
  }

  public static MoveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    // resolution is special for this command
    return new MoveCommand(actor, player, commandArgs.get(0));
  }
}
