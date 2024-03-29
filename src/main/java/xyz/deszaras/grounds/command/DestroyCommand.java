package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Destroys a thing.<p>
 *
 * Arguments: thing
 */
@PermittedRoles(roles = { Role.BARD, Role.THAUMATURGE },
                failureMessage = "You are not a bard or thaumaturge, so you may not destroy")
public class DestroyCommand extends Command<Boolean> {

  private final Thing thing;

  public DestroyCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @VisibleForTesting
  public Thing getThing() {
    return thing;
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  @Override
  protected Boolean executeImpl() throws CommandException {
    String whyNot = mayDestroy(thing);
    if (whyNot != null) {
      throw new CommandException(String.format("This thing cannot be destroyed: %s",
                                               whyNot));
    }

    Place laf = Universe.getCurrent().getLostAndFoundPlace();
    for (UUID contentThingId : thing.getContents()) {
      Thing contentThing = Universe.getCurrent().getThing(contentThingId).get();
      thing.take(contentThing);
      laf.give(contentThing);
      contentThing.setLocation(laf);
    }

    try {
      if (thing.getLocation().isPresent()) {
        thing.getLocation().get().take(thing);
      }
    } catch (MissingThingException e) {
      // no problem
    }
    if (thing.getClass().equals(Player.class)) {
      Universe.getCurrent().removeAllRoles((Player) thing);
    } else if (thing.getClass().equals(Extension.class)) {
      CommandExecutor.getInstance().getCommandEventBus().unregister(thing);
    }
    Universe.getCurrent().removeThing(thing);

    player.sendMessage(newInfoMessage("Destroyed " + thing.getId()));
    return true;
  }

  private String mayDestroy(Thing thing) {
    if (!thing.passes(Category.WRITE, player)) {
      return "You lack WRITE permission to this, so you may not destroy it";
    }

    Class<? extends Thing> thingClass = thing.getClass();
    if (thingClass.equals(Extension.class) ||
        thingClass.equals(Link.class) ||
        thingClass.equals(Thing.class)) {
      return null;
    }
    if (thingClass.equals(Place.class)) {
      if (!thing.getContents().isEmpty()) {
        return "This place is occupied. Empty it out first";
      }
      Place place = (Place) thing;
      if (Universe.getCurrent().getThings(Link.class).stream()
          .anyMatch(l -> l.linksTo(place))) {
        return "There are still links to this place. Unlink or remove them first";
      }
      return null;
    }
    if (thingClass.equals(Player.class)) {
      Player playerToDestroy = (Player) thing;
      if (playerToDestroy.getCurrentActor().isPresent()) {
        return "Someone is currently playing as that player";
      }
      return null;
    }

    return "I don't know how to safely destroy this ... yet";
  }

  public static DestroyCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing thingToDestroy =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new DestroyCommand(actor, player, thingToDestroy);
  }
}
