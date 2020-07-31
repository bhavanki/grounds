package xyz.deszaras.grounds.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;

@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class LookCommand extends Command<String> {

  public LookCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    try {
      Optional<Place> location = player.getLocationAsPlace();
      if (location.isPresent()) {
        checkPermission(Category.READ, location.get(),
                        "You are not permitted to look at where you are");
        return buildMessage(location.get());
      } else {
        return "nowhere";
      }
    } catch (MissingThingException e) {
      throw new CommandException("I cannot determine your location!");
    }
  }

  private String buildMessage(Place location) {
    String name = AnsiUtils.color(location.getName(), Ansi.Color.CYAN, false);
    StringBuilder b = new StringBuilder(name);
    Optional<String> description = location.getDescription();
    if (description.isPresent()) {
      b.append("\n\n").append(description.get());
    }

    List<Player> players = new ArrayList<>();
    List<Thing> theRest = new ArrayList<>();

    location.getContents().forEach(id -> {
      Optional<Thing> t = Universe.getCurrent().getThing(id);
      if (t.isPresent()) {
        Thing tt = t.get();
        if (tt instanceof Player) {
          players.add((Player) tt);
        } else if (!(tt instanceof Extension) ||
                   checkIfAnyRole(Extension.PERMITTED_ROLES)) {
          theRest.add(tt);
        }
      }
    });

    Collections.sort(players, (p1, p2) -> p1.getName().compareTo(p2.getName()));
    Collections.sort(theRest, (t1, t2) -> t1.getName().compareTo(t2.getName()));

    boolean showId =
        Boolean.parseBoolean(actor.getPreference(Actor.PREFERENCE_SHOW_IDS).orElse("false"));

    if (players.size() > 0) {
      b.append("\n\n" + AnsiUtils.color("Players present:", Ansi.Color.CYAN, false));
      for (Player p : players) {
        b.append("\n- " + AnsiUtils.listing(p, showId));
      }
    }
    if (theRest.size() > 0) {
      b.append("\n\n" + AnsiUtils.color("Contents:", Ansi.Color.CYAN, false));
      for (Thing t : theRest) {
        b.append("\n- " + AnsiUtils.listing(t, showId));
      }
    }

    Collection<Link> links = Universe.getCurrent().findLinks(location);
    if (!links.isEmpty()) {
      b.append("\n\n" + AnsiUtils.color("Exits:", Ansi.Color.CYAN, false));
      for (Link link : links) {
        Optional<Attr> otherPlaceAttr = link.getOtherPlace(location);
        if (otherPlaceAttr.isPresent()) {
          Optional<Place> otherPlace =
              Universe.getCurrent().getThing(otherPlaceAttr.get().getThingValue(),
                                             Place.class);
          if (otherPlace.isPresent()) {
            String otherPlaceName =
                AnsiUtils.color(otherPlaceAttr.get().getName(), Ansi.Color.GREEN, false);
            b.append("\n- (" + otherPlaceName + ") " + AnsiUtils.listing(otherPlace.get(), showId));
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
