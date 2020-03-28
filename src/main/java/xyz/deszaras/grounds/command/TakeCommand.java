package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class TakeCommand extends Command {

  private final Thing thing;

  public TakeCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public boolean execute() {
    if (player.has(thing)) {
      actor.sendMessage("You are already holding that");
      return false;
    }
    if (!thing.passes(Category.GENERAL, player)) {
      actor.sendMessage("You are unable to take that");
      return false;
    }

    Optional<Place> location = thing.getLocation();
    if (!Role.isWizard(player, thing.getUniverse())) {
      if (location.isEmpty() ||             // the thing has no location
          player.getLocation().isEmpty() || // the player has no location
          !location.get().equals(player.getLocation().get())) {
        actor.sendMessage("You may only take that if you are in the same location");
      return false;
      }
    }

    if (location.isPresent()) {
      location.get().take(thing);
    }
    player.give(thing);
    return true;
  }

  public static TakeCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing droppedThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new TakeCommand(actor, player, droppedThing);
  }
}
