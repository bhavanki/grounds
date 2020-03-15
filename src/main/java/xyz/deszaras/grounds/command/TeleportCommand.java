package xyz.deszaras.grounds.command;

import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class TeleportCommand extends Command {

  private final Place destination;

  public TeleportCommand(Actor actor, Player player, Place destination) {
    super(actor, player);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  public boolean execute() {
    Optional<Attr> locationAttr = player.getAttr(AttrNames.LOCATION);
    if (locationAttr.isPresent()) {
      Optional<Place> source = locationAttr.get().getThingValue(Place.class);
      if (source.isPresent()) {
        source.get().take(player);
        source.get().getUniverse().removeThing(player);
      }
    }
    destination.give(player);
    player.setAttr(AttrNames.LOCATION, destination);
    player.setUniverse(destination.getUniverse());
    destination.getUniverse().addThing(player);

    new LookCommand(actor, player).execute();

    return true;
  }
}
