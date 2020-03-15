package xyz.deszaras.grounds.command;

import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class MoveCommand extends Command {

  private final Place destination;

  public MoveCommand(Actor actor, Player player, Place destination) {
    super(actor, player);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  public boolean execute() {
    Optional<Attr> locationAttr = player.getAttr(AttrNames.LOCATION);
    if (locationAttr.isPresent()) {
      Optional<Thing> source = locationAttr.get().getThingValue();
      if (source.isPresent()) {
        ((Place) source.get()).take(player);
      }
    }
    destination.give(player);
    player.setUniverse(destination.getUniverse());
    return true;
  }
}
