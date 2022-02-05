package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Creates a new thing.<p>
 *
 * Arguments: type of thing, name of thing, type-specific arguments<br>
 * Checks: type of thing is known; player is not in the VOID universe;
 * player has a location
 */
@PermittedRoles(roles = { Role.BARD, Role.THAUMATURGE },
                failureMessage = "You are not a bard or thaumaturge, so you may not build")
public class BuildCommand extends Command<String> {

  public enum BuiltInType {
    THING,
    PLAYER,
    PLACE,
    LINK,
    EXTENSION;
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
  protected String executeImpl() throws CommandException {
    BuiltInType thingType;
    try {
      thingType = BuiltInType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CommandException("I don't know how to build " + type);
    }
    if (Universe.getCurrent().getName().equals(Universe.VOID.getName())) {
      throw new CommandException("Building is not permitted in the VOID universe");
    }

    Place location;
    if (!(Player.GOD.equals(player))) {
      location = getPlayerLocation("build anything");
    } else {
      try {
        location = player.getLocationAsPlace().orElse(Universe.getCurrent().getOriginPlace());
      } catch (MissingThingException e) {
        location = Universe.getCurrent().getOriginPlace();
      }
    }

    Thing built;

    try {
      switch (thingType) {
        case THING:
          built = Thing.build(name, buildArgs);
          break;
        case PLAYER:
          if (Universe.getCurrent().getThingByName(name, Player.class).isPresent()) {
            throw new CommandException("A player named " + name + " already exists");
          }
          built = Player.build(name, buildArgs);
          break;
        case PLACE:
          built = Place.build(name, buildArgs);
          break;
        case LINK:
          built = Link.build(name, buildArgs);
          break;
        case EXTENSION:
          built = Extension.build(name, buildArgs);
          break;
        default:
          throw new IllegalArgumentException("Unsupported built-in type " + type);
      }
      Universe.getCurrent().addThing(built);
      if (thingType == BuiltInType.THING ||
          thingType == BuiltInType.PLAYER) {
        built.setLocation(location);
        location.give(built);
      } else if (thingType == BuiltInType.EXTENSION) {
        CommandExecutor.getInstance().getCommandEventBus().register(built);
      }
      player.sendMessage(newInfoMessage("Created " + built.getId()));
      return built.getId().toString();
    } catch (IllegalArgumentException e) {
      // Future: Dynamic types
      throw new CommandException("Unsupported type " + type + ": " + e.getMessage());
    }
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
