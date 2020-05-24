package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class DropCommand extends Command<Boolean> {

  private final Thing thing;

  public DropCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.has(thing)) {
      throw new CommandException("You aren't holding that");
    }
    // This next check is questionable
    if (!thing.passes(Category.GENERAL, player)) {
      throw new CommandException("You are unable to drop that");
    }
    Optional<Place> location = player.getLocation();
    if (location.isEmpty()) {
      throw new CommandException("You are not located anywhere, so you may not drop anything");
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
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new DropCommand(actor, player, droppedThing);
  }
}
