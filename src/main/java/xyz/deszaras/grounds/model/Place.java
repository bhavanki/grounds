package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Policy;

/**
 * A thing that represents a place in the world.
 */
public class Place extends Thing {

  public Place(String name) {
    super(name);
  }

  /**
   * Creates a new place.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Place(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);
  }

  public static Place build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 0, "Expected 0 build arguments, got " + buildArgs.size());
    return new Place(name);
  }
}
