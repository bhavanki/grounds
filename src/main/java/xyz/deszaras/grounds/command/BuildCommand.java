package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class BuildCommand extends Command {

  public enum BuiltInType {
    PLAYER,
    PLACE,
    LINK,
    UNIVERSE;
  }

  private final String type;
  private final List<String> buildArgs;

  public BuildCommand(Actor actor, Player player, String type,
                      List<String> buildArgs) {
    super(actor, player);
    this.type = Objects.requireNonNull(type);
    this.buildArgs = ImmutableList.copyOf(Objects.requireNonNull(buildArgs));
  }

  @Override
  public boolean execute() {
    Universe universe = player.getUniverse();
    Thing built;

    try {
      switch (BuiltInType.valueOf(type.toUpperCase())) {
        case PLAYER:
          built = Player.build(universe, buildArgs);
          break;
        case PLACE:
          built = Place.build(universe, buildArgs);
          break;
        case LINK:
          built = Link.build(universe, buildArgs);
          break;
        case UNIVERSE:
          // special case
          universe = Universe.build(buildArgs);
          Multiverse.MULTIVERSE.putUniverse(universe);
          actor.sendMessage("Created universe " + universe.getName());

          Place origin = new Place(universe, "origin");
          universe.addThing(origin);
          actor.sendMessage("Created origin place " + origin.getId());
          return true;
        default:
          throw new IllegalArgumentException("Unsupported built-in type " + type);
      }
      universe.addThing(built);
      actor.sendMessage("Created " + built.getId());
      return true;
    } catch (IllegalArgumentException e) {
      // Future: Dynamic types
      actor.sendMessage("Unsupported type " + type + ": " + e.getMessage());
    }
    return false;
  }

}
