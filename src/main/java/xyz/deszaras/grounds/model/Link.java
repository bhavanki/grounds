package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A thing that links one or more places in the world.
 */
public class Link extends Thing {

  public static final String SOURCE = "source";
  public static final String DESTINATION = "destination";

  public Link(String name, Universe universe, Place source, String sourceName,
              Place destination, String destinationName) {
    super(name, universe);

    setAttr(SOURCE, new Attr(sourceName, source));
    setAttr(DESTINATION, new Attr(destinationName, destination));
  }

  /**
   * Creates a new link.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the link has no
   * destinations attribute
   */
  @JsonCreator
  public Link(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents) {
    super(id, attrs, contents);

    if (!getAttr(SOURCE).isPresent()) {
      throw new IllegalArgumentException("Link is missing destination");
    }
    if (!getAttr(DESTINATION).isPresent()) {
      throw new IllegalArgumentException("Link is missing destination");
    }
  }

  @JsonIgnore
  public Optional<Place> getSource() {
    return getAttr(SOURCE).get().getAttrValue().getThingValue(Place.class);
  }

  @JsonIgnore
  public Optional<Place> getDestination() {
    return getAttr(DESTINATION).get().getAttrValue().getThingValue(Place.class);
  }

  @JsonIgnore
  public List<Optional<Place>> getPlaces() {
    return ImmutableList.of(getSource(), getDestination());
  }

  public boolean linksTo(Place place) {
    Optional<Place> placeOptional = Optional.of(place);
    return placeOptional.equals(getSource()) ||
        placeOptional.equals(getDestination());
  }

  public Optional<Attr> getOtherPlace(Place location) {
    Optional<Place> source = getSource();
    if (source.isPresent() && location.equals(source.get())) {
      return Optional.of(getAttr(DESTINATION).get().getAttrValue());
    }
    Optional<Place> destination = getDestination();
    if (destination.isPresent() && location.equals(destination.get())) {
      return Optional.of(getAttr(SOURCE).get().getAttrValue());
    }
    return Optional.empty();
  }

  // TBD: make changing its universe impossible

  public static Link build(String name, Universe universe, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 4, "Expected 4 build arguments, got " + buildArgs.size());
    Optional<Place> source = Multiverse.MULTIVERSE.findThing(buildArgs.get(0), Place.class);
    if (!source.isPresent()) {
      throw new IllegalArgumentException("Cannot find source " + buildArgs.get(0));
    }
    String sourceName = Objects.requireNonNull(buildArgs.get(1));
    Optional<Place> destination = Multiverse.MULTIVERSE.findThing(buildArgs.get(2), Place.class);
    if (!destination.isPresent()) {
      throw new IllegalArgumentException("Cannot find destination " + buildArgs.get(2));
    }
    String destinationName = Objects.requireNonNull(buildArgs.get(3));
    return new Link(name, universe, source.get(), sourceName,
                    destination.get(), destinationName);
  }
}
