package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;

/**
 * A world full of things. The universe is stored in memory.
 */
public class Universe {

  /**
   * The VOID universe, where there is NOTHING and EVERYTHING, and
   * where GOD lives.
   */
  public static final Universe VOID = new Universe("VOID");

  static {
    Multiverse.MULTIVERSE.putUniverse(VOID);
  }

  private final String name;
  private final Map<UUID, Thing> things;
  private final Map<UUID, Set<Role>> roles;

  /**
   * Creates an empty universe. Whoa.
   *
   * @param name name
   */
  public Universe(String name) {
    this.name = Objects.requireNonNull(name);
    things = new HashMap<>();
    roles = new HashMap<>();
  }

  /**
   * Creates a populated universe.
   *
   * @param name name
   * @param things things in the universe
   * @param roles player role assignments in the universe
   */
  @JsonCreator
  public Universe(
      @JsonProperty("name") String name,
      @JsonProperty("things") Set<Thing> things,
      @JsonProperty("roleAssignments") Map<String, Set<Role>> roles) {
    this(name);
    if (things != null) {
      things.stream().forEach(thing -> this.things.put(thing.getId(), thing));
    }
    if (roles != null) {
      roles.entrySet().stream()
          .forEach(entry -> this.roles.put(UUID.fromString(entry.getKey()),
                                           entry.getValue()));
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
   * Gets all the things in this universe of a specific type.
   *
   * @param thingClass thing class
   * @return things
   */
  @JsonIgnore
  public <T extends Thing> Collection<T> getThings(Class<T> thingClass) {
    return things.values().stream()
        .filter(t -> thingClass.equals(t.getClass()))
        .map(t -> thingClass.cast(t))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Gets a thing in this universe.
   *
   * @param id ID of thing to find
   * @return thing
   */
  public Optional<Thing> getThing(UUID id) {
    return Optional.ofNullable(things.get(id));
  }

  /**
   * Gets a thing in this universe, with an expected type.
   *
   * @param id ID of thing to find
   * @param thingClass expected type of thing
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public <T extends Thing> Optional<T> getThing(UUID id, Class<T> thingClass) {
    try {
      return getThing(id).map(t -> thingClass.cast(t));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Thing " + id.toString() + " not of expected type " +
                                         thingClass.getName(), e);
    }
  }

  /**
   * Gets a thing in this universe by name, with an expected type. If
   * multiple things share the name, an arbitrary one is returned.
   *
   * @param name name of thing to find
   * @param thingClass expected type of thing
   * @return thing
   */
  public <T extends Thing> Optional<T> getThingByName(String name, Class<T> thingClass) {
    // This is currently quite expensive
    try {
      return things.values().stream()
          .filter(t -> thingClass.isAssignableFrom(t.getClass()))
          .filter(t -> t.getName().equals(name))
          .findFirst()
          .map(t -> thingClass.cast(t));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Thing " + name + " not of expected type " +
                                         thingClass.getName(), e);
    }
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

  /**
   * Gets all the role assignments in this universe.
   *
   * @return role assignments
   */
  @JsonProperty
  public Map<UUID, Set<Role>> getRoleAssignments() {
    return Collections.unmodifiableMap(roles);
  }

  /**
   * Gets the roles for a player in this universe.
   *
   * @param player player
   * @return current roles
   */
  public Set<Role> getRoles(Player player) {
    return Collections.unmodifiableSet(roles.getOrDefault(player.getId(), Collections.emptySet()));
  }

  /**
   * Adds a role for a player in this universe.
   *
   * @param role role to add
   * @param player player
   * @return new set of current roles
   */
  public Set<Role> addRole(Role role, Player player) {
    return roles.merge(player.getId(), EnumSet.of(role),
                       (oldSet, newSet) -> {
                          oldSet.addAll(newSet);
                          return oldSet;
                        });
  }

  /**
   * Removes a role for a player in this universe.
   *
   * @param role role to remove
   * @param player player
   * @return new set of current roles
   */
    public Set<Role> removeRole(Role role, Player player) {
    return roles.computeIfPresent(player.getId(),
                                  (key, oldSet) -> {
                                    oldSet.remove(role);
                                    return oldSet;
                                  });
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

  /**
   * Builds a new universe from arguments. Expected arguments: none.
   *
   * @param name name
   * @param universe starting universe
   * @param buildArgs build arguments
   * @return new universe
   * @throws IllegalArgumentException if the number of arguments is wrong
   */
  public static Universe build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 0, "Expected 0 build arguments, got " + buildArgs.size());
    return new Universe(name);
  }
}
