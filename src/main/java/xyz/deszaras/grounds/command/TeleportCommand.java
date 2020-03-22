package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Teleports the player to a destination anywhere in the multiverse.<p>
 *
 * Arguments: ID of destination<br>
 * Checks: player passes GENERAL of destination
 */
public class TeleportCommand extends Command {

  private final Place destination;

  public TeleportCommand(Actor actor, Player player, Place destination) {
    super(actor, player);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  public boolean execute() {
    if (!destination.passes(Category.GENERAL, player)) {
      actor.sendMessage("You are not permitted to move there");
      return false;
    }

    Optional<Attr> locationAttr = player.getAttr(AttrNames.LOCATION);
    if (locationAttr.isPresent()) {
      Optional<Place> source = locationAttr.get().getThingValue(Place.class);
      if (source.isPresent()) {
        source.get().take(player);
        source.get().getUniverse().removeThing(player);
      }
    } else {
      player.getUniverse().removeThing(player);
    }

    destination.give(player);
    player.setAttr(AttrNames.LOCATION, destination);
    player.setUniverse(destination.getUniverse());
    destination.getUniverse().addThing(player);

    new LookCommand(actor, player).execute();

    return true;
  }

  public static TeleportCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Optional<Place> destination = Multiverse.MULTIVERSE.findThing(commandArgs.get(0), Place.class);
    if (!destination.isPresent()) {
      throw new CommandFactoryException("Failed to find destination in universe");
    }
    return new TeleportCommand(actor, player, destination.get());
  }
}
