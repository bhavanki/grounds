package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class DropCommand extends Command {

  private final Thing thing;

  public DropCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public boolean execute() {
    if (!player.has(thing)) {
      actor.sendMessage("You aren't holding that");
      return false;
    }
    // This next check is questionable
    if (!thing.passes(Category.GENERAL, player)) {
      actor.sendMessage("You are unable to drop that");
      return false;
    }
    Optional<Place> location = player.getLocation();
    if (location.isEmpty()) {
      actor.sendMessage("You are not located anywhere, so you may not drop anything");
      return false;
    }

    player.take(thing);
    location.get().give(thing);
    thing.setLocation(location.get());
    return true;
  }

  public static DropCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing droppedThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new DropCommand(actor, player, droppedThing);
  }
}
