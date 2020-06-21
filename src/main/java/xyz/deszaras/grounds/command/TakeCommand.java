package xyz.deszaras.grounds.command;

import static xyz.deszaras.grounds.util.TestabilityUtils.nonmock;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class TakeCommand extends Command<Boolean> {

  private final Thing thing;

  public TakeCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (player.has(thing)) {
      throw new CommandException("You are already holding that");
    }
    if (!(nonmock(thing.getClass()).equals(Thing.class))) {
      throw new CommandException("You can only take ordinary things");
    }
    checkPermission(Category.GENERAL, thing, "You are not permitted to take that");

    Optional<Place> thingLocation;
    try {
      thingLocation = thing.getLocation();
    } catch (MissingThingException e) {
      thingLocation = Optional.empty();
    }
    // A non-wizard must be in the same location as a thing to take it.
    // (A wizard may take a thing from anywhere, even nowhere.)
    if (!Role.isWizard(player)) {
      Optional<Place> playerLocation;
      try {
        playerLocation = player.getLocation();
      } catch (MissingThingException e) {
        playerLocation = Optional.empty();
      }
      if (thingLocation.isEmpty() ||   // the thing has no location
          playerLocation.isEmpty() ||  // the player has no location
          !thingLocation.get().equals(playerLocation.get())) {
        throw new CommandException("You may only take that if you are in the same location");
      }
    }

    if (thingLocation.isPresent()) {
      thingLocation.get().take(thing);
    }
    player.give(thing);
    thing.setLocation(null);
    return true;
  }

  public static TakeCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing droppedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new TakeCommand(actor, player, droppedThing);
  }
}
