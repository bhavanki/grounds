package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A world full of things. The universe is stored in memory.
 */
public class Universe {

  /**
   * The void universe, where there is nothing and everything, and
   * where GOD lives.
   */
  public static final Universe VOID = new Universe("void");

  static {
    Multiverse.MULTIVERSE.putUniverse(VOID);
  }

  private final String name;
  private final Map<UUID, Thing> things;

  /**
   * Creates an empty universe. Whoa.
   *
   * @param name name
   */
  public Universe(String name) {
    this.name = Objects.requireNonNull(name);
    things = new HashMap<>();
  }

  /**
   * Creates a populated universe.
   *
   * @param name name
   * @param things things in the universe
   */
  @JsonCreator
  public Universe(
      @JsonProperty("name") String name,
      @JsonProperty("things") Set<Thing> things) {
    this(name);
    if (things != null) {
      things.stream().forEach(thing -> this.things.put(thing.getId(), thing));
    }
  }

  /**
   * Gets the name of this universe.
   *
   * @return name
   */
  @JsonProperty
  public String getName() {
    return name;
  }

  /**
   * Gets all the things in this universe.
   *
   * @return things
   */
  @JsonProperty
  public Collection<Thing> getThings() {
    return Collections.unmodifiableCollection(things.values());
  }

  /**
   * Gets a thing in this universe.
   *
   * @param id
   * @return thing
   */
  public Optional<Thing> getThing(UUID id) {
    return Optional.ofNullable(things.get(id));
  }

  /**
   * Adds a thing to this universe.
   *
   * @param thing thing to add
   * @throws NullPointerException if thing is null
   */
  public void addThing(Thing thing) {
    Objects.requireNonNull(thing);
    things.put(thing.getId(), thing);
  }

  /**
   * Removes a thing from this universe.
   *
   * @param thing thing to remove (may be null)
   */
  public void removeThing(Thing thing) {
    if (thing != null) {
      things.remove(thing.getId());
    }
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Creates a JSON representation of this universe.
   *
   * @return JSON string
   * @throws IllegalStateException if conversion fails
   */
  @JsonIgnore
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to convert universe to JSON", e);
    }
  }

  /**
   * Creates a new universe from a JSON representation.
   *
   * @param s JSON string
   * @return universe
   * @throws IllegalArgumentException if conversion fails
   */
  public static Universe fromJson(String s) {
    try {
      return OBJECT_MAPPER.readValue(s, Universe.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to create universe from JSON", e);
    }
  }

  public static Universe build(List<String> buildArgs) {
    checkArgument(buildArgs.size() == 1, "Expected 1 build argument, got " + buildArgs.size());
    return new Universe(buildArgs.get(0));
  }
}
