package xyz.deszaras.grounds.command;

import static xyz.deszaras.grounds.util.TestabilityUtils.nonmock;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class TakeCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(TakeCommand.class);

  private final Thing thing;

  public TakeCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (player.has(thing)) {
      throw new CommandException("You are already holding that");
    }
    if (!(nonmock(thing.getClass()).equals(Thing.class))) {
      throw new CommandException("You can only take ordinary things");
    }
    if (!Role.isWizard(player)) {
      checkPermission(Category.GENERAL, thing, "You are not permitted to take that");
    }

    Optional<Thing> thingLocation;
    try {
      thingLocation = thing.getLocation();
    } catch (MissingThingException e) {
      thingLocation = Optional.empty();
    }
    Optional<Player> holder = findHolder(thingLocation);
    if (holder.isPresent() && holder.get().equals(player)) {
      throw new CommandException("You are already holding that");
    }

    // ADEPTS and THAUMATURGES may seize a thing from another player. A
    // BARD may not.
    if (Role.isWizard(player) && holder.isPresent()) {
      if (Universe.getCurrent().hasRole(Role.BARD, player)) {
        throw new PermissionException("You may not take something from " +
                                      "another player");
      }
      LOG.info("Player {} is seizing {} [{}] from {}",
               player.getName(), thing.getName(), thing.getId(),
               holder.get().getName());
    }

    if (!Role.isWizard(player)) {
      // A non-wizard must be in the same location as a thing to take it.
      // (A wizard may take a thing from anywhere, even nowhere.)
      Optional<Place> playerLocation;
      try {
        playerLocation = player.getLocationAsPlace();
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
    thing.setLocation(player);
    return true;
  }

  private static Optional<Player> findHolder(Optional<Thing> thingLocation) {
    Optional<Thing> loc = thingLocation;
    try {
      while (!loc.isEmpty() && nonmock(loc.get().getClass()).equals(Thing.class)) {
        loc = loc.get().getLocation();
      }
    } catch (MissingThingException e) {
      return Optional.empty();
    }
    return loc.isEmpty() ? Optional.empty() :
        Optional.ofNullable(loc.get() instanceof Player ? (Player) loc.get() : null);
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
