package xyz.deszaras.grounds.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class LookCommand extends Command<String> {

  public LookCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public String execute() throws CommandException {
    Optional<Place> location = player.getLocation();
    if (location.isPresent()) {
      checkPermission(Category.READ, location.get(),
                      "You are not permitted to look at where you are");
      return buildMessage(location.get());
    } else {
      return "nowhere";
    }
  }

  private String buildMessage(Place location) {
    StringBuilder b = new StringBuilder(location.getName());
    Optional<String> description = location.getDescription();
    if (description.isPresent()) {
      b.append("\n\n").append(description.get());
    }
    if (location.getContents().size() > 0) {
      b.append("\n\nCONTENTS:");
      location.getContents().forEach(id -> {
        Optional<Thing> t = Multiverse.MULTIVERSE.findThing(id);
        if (t.isPresent()) {
          b.append("\n- " + t.get().getName () + " [" + t.get().getId() + "]");
        }
      });
    }
    Collection<Link> links = Multiverse.MULTIVERSE.findLinks(location);
    if (!links.isEmpty()) {
      b.append("\n\nEXITS:");
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

  public static String help() {
    return "LOOK\n\n" +
        "Describes where you are, including other things present and exits";
  }
}
