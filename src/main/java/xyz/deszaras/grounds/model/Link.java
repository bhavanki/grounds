package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.util.UUIDUtils;

/**
 * A thing that links one or more places in the world.
 */
public class Link extends Thing {

  public static final String SOURCE = "source";
  public static final String DESTINATION = "destination";

  public Link(String name, Place source, String sourceName,
              Place destination, String destinationName) {
    super(name);

    setAttr(SOURCE, new Attr(sourceName, source));
    setAttr(DESTINATION, new Attr(destinationName, destination));
  }

  /**
   * Creates a new link.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if the link has no
   * destinations attribute
   */
  @JsonCreator
  public Link(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);

    if (!getAttr(SOURCE).isPresent()) {
      throw new IllegalArgumentException("Link is missing destination");
    }
    if (!getAttr(DESTINATION).isPresent()) {
      throw new IllegalArgumentException("Link is missing destination");
    }
  }

  @JsonIgnore
  public Optional<Place> getSource() {
    String sourceId = getAttr(SOURCE).get().getAttrValue().getThingValue();
    return Universe.getCurrent().getThing(UUIDUtils.getUUID(sourceId), Place.class);
  }

  @JsonIgnore
  public Optional<Place> getDestination() {
    String destinationId = getAttr(DESTINATION).get().getAttrValue().getThingValue();
    return Universe.getCurrent().getThing(UUIDUtils.getUUID(destinationId), Place.class);
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

  public static Link build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 4, "Expected 4 build arguments, got " + buildArgs.size());
    if (!UUIDUtils.isUUID(buildArgs.get(0))) {
      throw new IllegalArgumentException("Not a UUID: " + buildArgs.get(0));
    }
    if (!UUIDUtils.isUUID(buildArgs.get(2))) {
      throw new IllegalArgumentException("Not a UUID: " + buildArgs.get(2));
    }
    Optional<Place> source =
        Universe.getCurrent().getThing(UUIDUtils.getUUID(buildArgs.get(0)), Place.class);
    if (!source.isPresent()) {
      throw new IllegalArgumentException("Cannot find source " + buildArgs.get(0));
    }
    String sourceName = Objects.requireNonNull(buildArgs.get(1));
    Optional<Place> destination =
        Universe.getCurrent().getThing(UUIDUtils.getUUID(buildArgs.get(2)), Place.class);
    if (!destination.isPresent()) {
      throw new IllegalArgumentException("Cannot find destination " + buildArgs.get(2));
    }
    String destinationName = Objects.requireNonNull(buildArgs.get(3));
    return new Link(name, source.get(), sourceName,
                    destination.get(), destinationName);
  }
}
