package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A thing that represents a place in the world.
 */
public class Place extends Thing {

  public Place(String name, Universe universe) {
    super(name, universe);
  }

  /**
   * Creates a new place.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Place(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents) {
    super(id, attrs, contents);
  }

  // TBD: make changing its universe impossible

  public static Place build(String name, Universe universe, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 0, "Expected 0 build arguments, got " + buildArgs.size());
    return new Place(name, universe);
  }
}
