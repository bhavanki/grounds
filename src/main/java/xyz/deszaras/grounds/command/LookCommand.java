package xyz.deszaras.grounds.command;

import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class LookCommand extends Command {

  public LookCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    Optional<Attr> locationAttr = player.getAttr(AttrNames.LOCATION);
    if (locationAttr.isPresent()) {
      Optional<Place> location = locationAttr.get().getThingValue(Place.class);
      if (location.isPresent()) {
        actor.sendMessage(location.get().getName());
      } else {
        actor.sendMessage("Odd, I can't find where you are");
      }
    } else {
      actor.sendMessage("nowhere");
    }
    return true;
  }
}
