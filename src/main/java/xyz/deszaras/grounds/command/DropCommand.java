package xyz.deszaras.grounds.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class DropCommand extends Command<Boolean> {

  private final Thing thing;

  public DropCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (!player.has(thing)) {
      throw new CommandException("You aren't holding that");
    }
    // This next check is questionable
    checkPermission(Category.GENERAL, thing, "You are unable to drop that");
    Place location = getPlayerLocation("drop anything");

    player.take(thing);
    location.give(thing);
    thing.setLocation(location);
    postEvent(new DroppedThingEvent(player, location, thing));
    return true;
  }

  public static DropCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing droppedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new DropCommand(actor, player, droppedThing);
  }

  /**
   * The payload for {@link DroppedThingEvent}.
   */
  public static class DroppedThing {
    /**
     * The name of what was dropped.
     */
    @JsonProperty
    public final String thingName;
    /**
     * The ID of what was dropped.
     */
    @JsonProperty
    public final String thingId;

    DroppedThing(Thing droppedThing) {
      thingName = droppedThing.getName();
      thingId = droppedThing.getId().toString();
    }
  }

  /**
   * An event posted when someone drops a thing. The event location refers to
   * where the thing was dropped to.
   */
  public static class DroppedThingEvent extends Event<DroppedThing> {
    DroppedThingEvent(Player player, Thing location, Thing droppedThing) {
      super(player, location, new DroppedThing(droppedThing));
    }
  }
}
