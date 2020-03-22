package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Creates a new thing in the player's current universe.<p>
 *
 * Arguments: type of thing, name of thing, type-specific arguments<br>
 * Checks: type of thing is known; player is not in the VOID universe
 * and creating something other than a new universe; player is a wizard;
 * player has a location if creating something other than a new universe
 */
public class BuildCommand extends Command {

  public enum BuiltInType {
    THING,
    PLAYER,
    PLACE,
    LINK,
    UNIVERSE;
  }

  private final String type;
  private final String name;
  private final List<String> buildArgs;

  public BuildCommand(Actor actor, Player player, String type,
                      String name, List<String> buildArgs) {
    super(actor, player);
    this.type = Objects.requireNonNull(type);
    this.name = Objects.requireNonNull(name);
    this.buildArgs = ImmutableList.copyOf(Objects.requireNonNull(buildArgs));
  }

  @Override
  public boolean execute() {
    Universe universe = player.getUniverse();

    BuiltInType thingType;
    try {
      thingType = BuiltInType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      actor.sendMessage("I don't know how to build " + type);
      return false;
    }
    if (thingType != BuiltInType.UNIVERSE &&
        universe.getName().equals(Universe.VOID.getName())) {
      actor.sendMessage("Building of anything except universes is not permitted in the VOID universe");
      return false;
    }

    if (!Role.isWizard(player)) {
      actor.sendMessage("You are not a wizard in this universe, so you may not build");
      return false;
    }

    if (thingType != BuiltInType.UNIVERSE && !player.getLocation().isPresent()) {
      actor.sendMessage("You are not located anywhere, so you may only build a universe");
      return false;
    }

    Thing built;

    try {
      switch (thingType) {
        case THING:
          built = Thing.build(name, universe, buildArgs);
          break;
        case PLAYER:
          if (Multiverse.MULTIVERSE.findThingByName(name, Player.class).isPresent()) {
            actor.sendMessage("A player named " + name + " already exists");
            return false;
          }
          built = Player.build(name, universe, buildArgs);
          break;
        case PLACE:
          built = Place.build(name, universe, buildArgs);
          break;
        case LINK:
          built = Link.build(name, universe, buildArgs);
          break;
        case UNIVERSE:
          if (Multiverse.MULTIVERSE.hasUniverse(name)) {
            actor.sendMessage("A universe named " + name + " already exists");
            return false;
          }
          universe = Universe.build(name, buildArgs);
          Multiverse.MULTIVERSE.putUniverse(universe);
          actor.sendMessage("Created universe " + universe.getName());

          Place origin = new Place("ORIGIN", universe);
          universe.addThing(origin);
          actor.sendMessage("Created origin place " + origin.getId());
          return true;
        default:
          throw new IllegalArgumentException("Unsupported built-in type " + type);
      }
      universe.addThing(built);
      built.setUniverse(universe);
      if (thingType == BuiltInType.THING ||
          thingType == BuiltInType.PLAYER) {
        built.setAttr(AttrNames.LOCATION, player.getLocation().get());
        player.getLocation().get().give(built);
      }
      actor.sendMessage("Created " + built.getId());
      return true;
    } catch (IllegalArgumentException e) {
      // Future: Dynamic types
      actor.sendMessage("Unsupported type " + type + ": " + e.getMessage());
    }
    return false;
  }

  public static BuildCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    String type = commandArgs.get(0);
    String name = commandArgs.get(1);
    List<String> buildArgs = commandArgs.subList(2, commandArgs.size());
    return new BuildCommand(actor, player, type, name, buildArgs);
  }
}
