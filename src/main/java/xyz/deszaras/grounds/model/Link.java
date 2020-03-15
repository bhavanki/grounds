package xyz.deszaras.grounds.model;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A thing that links one or more places in the world.
 */
public class Link extends Thing {

  public static final String DESTINATIONS = "destinations";

  public Link(String name, Universe universe, List<Place> places) {
    super(name, universe);

    List<Attr> destList = new ArrayList<>();
    places.forEach(place -> destList.add(new Attr(place.getName(), place)));

    setAttr(DESTINATIONS, destList);
  }

  // TBD JSON creator constructor

  public List<Place> getPlaces() {
    return ImmutableList.copyOf(
      getAttr(DESTINATIONS).get().getAttrListValue().stream()
        .map(a -> {
          Optional<Place> place = a.getThingValue(Place.class);
          if (place.isPresent()) {
            return place.get();
          } else {
            return (Place) null;
          }
        })
        .filter(p -> p != null)
        .collect(Collectors.toList())
    );
  }

  // TBD: make changing its universe impossible

  public static Link build(String name, Universe universe, List<String> buildArgs) {
    List<Place> places = new ArrayList<>();
    for (String buildArg : buildArgs) {
      Optional<Place> place = Multiverse.MULTIVERSE.findThing(buildArg, Place.class);
      if (!place.isPresent()) {
        throw new IllegalArgumentException("Cannot find place " + buildArg);
      }
      places.add(place.get());
    }
    return new Link(name, universe, places);
  }
}
