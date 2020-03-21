package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class MoveCommand extends Command {

  private final String exitName;

  public MoveCommand(Actor actor, Player player, String exitName) {
    super(actor, player);
    this.exitName = Objects.requireNonNull(exitName);
  }

  @Override
  public boolean execute() {
    Optional<Attr> locationAttr = player.getAttr(AttrNames.LOCATION);
    if (!locationAttr.isPresent()) {
      actor.sendMessage("You have no current location, so you cannot move elsewhere");
      return false;
    }
    Optional<Place> source = locationAttr.get().getThingValue(Place.class);
    if (!source.isPresent()) {
      actor.sendMessage("You have a location, but I can't find that place!");
      return false;
    }

    // Check that there is a link associated with the player's current
    // location whose other place name matches the exit name. That other
    // place is the move destination.
    Optional<Place> moveDestination = null;
    Optional<Link> viaLink = null;
    for (Link link : Multiverse.MULTIVERSE.findLinks(source.get())) {
      Optional<Attr> otherPlace = link.getOtherPlace(source.get());
      if (otherPlace.isPresent() &&
          otherPlace.get().getName().equalsIgnoreCase(exitName)) {
        moveDestination = otherPlace.get().getThingValue(Place.class);
        viaLink = Optional.of(link);
      }
    }
    if (moveDestination == null) {
      actor.sendMessage("I can't see an exit named " + exitName);
      return false;
    }
    if (!moveDestination.isPresent()) {
      actor.sendMessage("The exit has another side, but I can't find that place!");
      return false;
    }
    if (!viaLink.get().passes(Category.USE, player)) {
      actor.sendMessage("You are not permitted to traverse the exit to that place");
      return false;
    }

    return new TeleportCommand(actor, player, moveDestination.get()).execute();
  }

  public static MoveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    return new MoveCommand(actor, player, commandArgs.get(0));
  }
}
