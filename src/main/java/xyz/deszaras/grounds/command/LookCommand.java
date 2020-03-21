package xyz.deszaras.grounds.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
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
        if (!location.get().passes(Category.READ, player)) {
          actor.sendMessage("You are not permitted to look at where you are");
          return false;
        }
        actor.sendMessage(buildMessage(location.get()));
      } else {
        actor.sendMessage("Odd, I can't find where you are");
      }
    } else {
      actor.sendMessage("nowhere");
    }
    return true;
  }

  private String buildMessage(Place location) {
    StringBuilder b = new StringBuilder(location.getName());
    Optional<String> description = location.getDescription();
    if (description.isPresent()) {
      b.append("\n\n").append(description.get());
    }
    Collection<Link> links = Multiverse.MULTIVERSE.findLinks(location);
    if (!links.isEmpty()) {
      b.append("\n\nEXITS:");
      Set<Optional<Place>> exits = new HashSet<>();
      for (Link link : links) {
        Optional<Attr> otherPlaceAttr = link.getOtherPlace(location);
        if (otherPlaceAttr.isPresent()) {
          String otherPlaceName = otherPlaceAttr.get().getName();
          Optional<Place> otherPlace = otherPlaceAttr.get().getThingValue(Place.class);
          if (otherPlace.isPresent()) {
            b.append("\n- (" + otherPlaceName + ") " +
                     otherPlace.get().getName() + " [" + otherPlace.get().getId() + "]");
          }
        }
      }
    }
    return b.toString();
  }

  public static LookCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs) {
    return new LookCommand(actor, player);
  }
}
