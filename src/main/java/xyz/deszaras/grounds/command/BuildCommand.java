package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
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
public class BuildCommand extends Command<Boolean> {

  public enum BuiltInType {
    THING,
    PLAYER,
    PLACE,
    LINK,
    EXTENSION,
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
  public Boolean execute() throws CommandException {
    Universe universe = player.getUniverse();

    BuiltInType thingType;
    try {
      thingType = BuiltInType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CommandException("I don't know how to build " + type);
    }
    if (thingType != BuiltInType.UNIVERSE &&
        universe.getName().equals(Universe.VOID.getName())) {
      throw new CommandException("Building of anything except universes is not permitted in the VOID universe");
    }

    if (!Role.isWizard(player)) {
      throw new PermissionException("You are not a wizard in this universe, so you may not build");
    }

    if (thingType != BuiltInType.UNIVERSE && !player.getLocation().isPresent()) {
      throw new CommandException("You are not located anywhere, so you may only build a universe");
    }

    Thing built;

    try {
      switch (thingType) {
        case THING:
          built = Thing.build(name, universe, buildArgs);
          break;
        case PLAYER:
          if (Multiverse.MULTIVERSE.findThingByName(name, Player.class).isPresent()) {
            throw new CommandException("A player named " + name + " already exists");
          }
          built = Player.build(name, universe, buildArgs);
          break;
        case PLACE:
          built = Place.build(name, universe, buildArgs);
          break;
        case LINK:
          built = Link.build(name, universe, buildArgs);
          break;
        case EXTENSION:
          built = Extension.build(name, universe, buildArgs);
          break;
        case UNIVERSE:
          if (Multiverse.MULTIVERSE.hasUniverse(name)) {
            throw new CommandException("A universe named " + name + " already exists");
          }
          universe = Universe.build(name, buildArgs);
          Multiverse.MULTIVERSE.putUniverse(universe);
          actor.sendMessage(newInfoMessage("Created universe " + universe.getName()));

          createOrigin(universe);
          createLostAndFound(universe);

          return true;
        default:
          throw new IllegalArgumentException("Unsupported built-in type " + type);
      }
      universe.addThing(built);
      built.setUniverse(universe);
      if (thingType == BuiltInType.THING ||
          thingType == BuiltInType.PLAYER) {
        built.setLocation(player.getLocation().get());
        player.getLocation().get().give(built);
      }
      actor.sendMessage(newInfoMessage("Created " + built.getId()));
      return true;
    } catch (IllegalArgumentException e) {
      // Future: Dynamic types
      throw new CommandException("Unsupported type " + type + ": " + e.getMessage());
    }
  }

  private void createOrigin(Universe universe) {
    Place origin = new Place("ORIGIN", universe);
    universe.addThing(origin);

    origin.setDescription(
        "This is the first place to exist in its new universe. From here" +
        " you can start building more things to create a new world. Type" +
        " `build help` to see what you can create.");

    actor.sendMessage(newInfoMessage("Created origin place " + origin.getId()));
  }

  private void createLostAndFound(Universe universe) {
    Place laf = new Place("LOST+FOUND", universe);
    universe.addThing(laf);
    universe.setLostAndFoundId(laf.getId());

    laf.setDescription(
        "This is where the contents of destroyed things end up.");

    actor.sendMessage(newInfoMessage("Created lost+found place " + laf.getId()));
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
