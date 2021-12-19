package xyz.deszaras.grounds.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Forcibly moves a thing to a destination anywhere in the universe.<p>
 *
 * Arguments: thing to yoink, ID of destination
 */
@PermittedRoles(roles = { Role.THAUMATURGE },
                failureMessage = "You are not a thaumaturge, so you may not yoink")
public class YoinkCommand extends Command<Boolean> {

  private final Thing yoinkedThing;
  private final Place destination;

  public YoinkCommand(Actor actor, Player player, Thing yoinkedThing,
                      Place destination) {
    super(actor, player);
    this.yoinkedThing = Objects.requireNonNull(yoinkedThing);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  protected Boolean executeImpl() {
    Optional<Thing> source;
    try {
      source = yoinkedThing.getLocation();
    } catch (MissingThingException e) {
      source = Optional.empty();
    }
    if (source.isPresent()) {
      Thing sourceThing = source.get();
      sourceThing.take(yoinkedThing);
      postEvent(new YoinkDepartureEvent(yoinkedThing, sourceThing));
      if (sourceThing instanceof Place &&
          yoinkedThing instanceof Player) {
        emitToAllPlayers(Optional.of((Place) sourceThing),
                         ((Player) yoinkedThing).getName() + " departs.");
      }
    }

    if (yoinkedThing instanceof Player) {
      emitToAllPlayers(Optional.of(destination),
                       ((Player) yoinkedThing).getName() + " arrives.");
    }
    destination.give(yoinkedThing);
    yoinkedThing.setLocation(destination);
    postEvent(new YoinkArrivalEvent(yoinkedThing, destination));
    if (yoinkedThing instanceof Player) {
      ((Player) yoinkedThing).sendMessage(new Message(player, Message.Style.INFO,
                                                      "You have been relocated to " +
                                                      destination.getName()));
    }

    return true;
  }

  public static YoinkCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing yoinkedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    Place destination =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(1), Place.class, player);
    return new YoinkCommand(actor, player, yoinkedThing, destination);
  }

  /**
   * The payload for {@link YoinkDepartureEvent}.
   */
  public static class YoinkDeparture {
    /**
     * The name of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingName;
    /**
     * The ID of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingId;
    /**
     * The type of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingType;

    YoinkDeparture(Thing yoinkedThing) {
      yoinkedThingName = yoinkedThing.getName();
      yoinkedThingId = yoinkedThing.getId().toString();
      yoinkedThingType = yoinkedThing.getClass().getSimpleName();
    }
  }

  /**
   * An event posted when a thing is yoinked from a source.
   */
  public static class YoinkDepartureEvent extends Event<YoinkDeparture> {
    YoinkDepartureEvent(Thing yoinkedThing, Thing source) {
      super(null, source, new YoinkDeparture(yoinkedThing));
    }
  }

  /**
   * The payload for {@link YoinkArrivalEvent}.
   */
  public static class YoinkArrival {
    /**
     * The name of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingName;
    /**
     * The ID of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingId;
    /**
     * The type of what was yoinked.
     */
    @JsonProperty
    public final String yoinkedThingType;

    YoinkArrival(Thing yoinkedThing) {
      yoinkedThingName = yoinkedThing.getName();
      yoinkedThingId = yoinkedThing.getId().toString();
      yoinkedThingType = yoinkedThing.getClass().getSimpleName();
    }
  }

  /**
   * An event posted when a thing is yoinked to a destination.
   */
  public static class YoinkArrivalEvent extends Event<YoinkArrival> {
    YoinkArrivalEvent(Thing yoinkedThing, Place destination) {
      super(null, destination, new YoinkArrival(yoinkedThing));
    }
  }
}
