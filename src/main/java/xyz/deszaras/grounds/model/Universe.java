package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.util.UUIDUtils;

/**
 * A world full of things. The universe is stored in memory.
 */
public class Universe {

  /**
   * The VOID universe, where there is NOTHING and EVERYTHING, and
   * where GOD lives.
   */
  public static final Universe VOID = new Universe("VOID");

  /**
   * The currently loaded universe.
   */
  public static Universe theUniverse;

  /**
   * The file where the currently loaded universe is loaded from / saved to.
   */
  private static File theUniverseFile;

  /**
   * Gets the current universe.
   *
   * @return current universe
   */
  public static Universe getCurrent() {
    return theUniverse;
  }

  /**
   * Sets the current universe.
   *
   * @param universe current universe
   */
  public static void setCurrent(Universe universe) {
    theUniverse = universe;
  }

  /**
   * Sets the current universe's file.
   *
   * @param universeFile current universe's file
   */
  public static void setCurrentFile(File universeFile) {
    theUniverseFile = universeFile;
  }

  /**
   * Saves the current universe to its file.
   *
   * @return true if the current universe was saved; false if there
   *         is no current universe, or it's the VOID universe, or
   *         if the current universe has no file
   * @throws IOException if the universe could not be saved
   * @see #save(Universe,File)
   */
  public static boolean saveCurrent() throws IOException {
    if (theUniverse == null || Universe.VOID.equals(theUniverse)) {
      return false;
    }
    if (theUniverseFile == null) {
      return false; // maybe should throw an exception
    }
    Universe.save(theUniverse, theUniverseFile);
    return true;
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Loads a universe from a file.
   *
   * @param  f           file containing universe
   * @return             loaded universe
   * @throws IOException if the universe could not be loaded
   */
  public static Universe load(File f) throws IOException {
    try {
      return OBJECT_MAPPER.readValue(f, Universe.class);
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to load universe", e);
    }
  }

  /**
   * Saves a universe to a file.
   *
   * @param  universe    universe to save
   * @param  f           file to save to
   * @throws IOException if the universe could not be saved
   */
  public static void save(Universe universe, File f) throws IOException {
    try {
      OBJECT_MAPPER.writeValue(f, universe);
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to save universe", e);
    }
  }

  private final String name;
  private final Map<UUID, Thing> things;
  private final Map<UUID, Set<Role>> roles;
  private UUID lostAndFoundId;
  private UUID guestHomeId;

  /**
   * Creates an empty universe. Whoa.
   *
   * @param name name
   */
  public Universe(String name) {
    this.name = Objects.requireNonNull(name);
    things = new HashMap<>();
    roles = new HashMap<>();
    lostAndFoundId = null;
    guestHomeId = null;
  }

  /**
   * Creates a populated universe.
   *
   * @param name name
   * @param things things in the universe
   * @param roles player role assignments in the universe
   * @param lostAndFoundId the ID for the lost and found place in the universe
   * @param guestHomeId the ID for the guest home in the universe
   */
  @JsonCreator
  public Universe(
      @JsonProperty("name") String name,
      @JsonProperty("things") Set<Thing> things,
      @JsonProperty("roleAssignments") Map<String, Set<Role>> roles,
      @JsonProperty("lostAndFoundId") String lafId,
      @JsonProperty("guestHomeId") String ghId) {
    this(name);
    if (things != null) {
      things.stream().forEach(thing -> this.things.put(thing.getId(), thing));
    }
    if (roles != null) {
      roles.entrySet().stream()
          .forEach(entry -> this.roles.put(UUID.fromString(entry.getKey()),
                                           entry.getValue()));
    }
    if (lafId != null) {
      lostAndFoundId = UUID.fromString(lafId);
    }
    if (ghId != null) {
      guestHomeId = UUID.fromString(ghId);
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
   * @param id string ID of thing to find
   * @return thing
   * @throws IllegalArgumentException if id is not a valid UUID
   */
  public Optional<Thing> getThing(String id) {
    return getThing(UUIDUtils.getUUID(id));
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
   * @param id string ID of thing to find
   * @param thingClass expected type of thing
   * @return thing
   * @throws IllegalArgumentException if id is not a valid UUID
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public <T extends Thing> Optional<T> getThing(String id, Class<T> thingClass) {
    return getThing(UUIDUtils.getUUID(id), thingClass);
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
   * Finds all links connected to the given place.
   *
   * @param  place place
   * @return       links that connect to the place
   */
  public Collection<Link> findLinks(Place place) {
    // this is inefficient
    return getThings(Link.class).stream()
        .filter(l -> l.linksTo(place))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Finds a link that connects the given source and destination (order isn't
   * important). If multiple links connect the two places, an arbitrary one is
   * returned.
   *
   * @param  source      place
   * @param  destination another place
   * @return             link between the two places
   */
  public Optional<Link> findLink(Place source, Place destination) {
    // this is inefficient
    return getThings(Link.class).stream()
        .filter(l -> l.linksTo(source) && l.linksTo(destination))
        .findFirst();
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
   * Checks if a player has a role in this universe.
   *
   * @param  role   role to check for
   * @param  player player
   * @return        true if player has role
   */
  public boolean hasRole(Role role, Player player) {
    return roles.getOrDefault(player.getId(), Collections.emptySet())
        .contains(role);
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

  /**
   * Removes all player roles in this universe.
   *
   * @param player player
   */
  public void removeAllRoles(Player player) {
    roles.remove(player.getId());
  }

  /**
   * Gets the ID for the lost and found place in this universe. This place is
   * where the contents of destroyed things go.
   *
   * @return ID of lost and found place
   */
  public UUID getLostAndFoundId() {
    return lostAndFoundId;
  }

  /**
   * Gets the lost and found place in this universe. This place is where the
   * contents of destroyed things go.
   *
   * @return lost and found place
   */
  @JsonIgnore
  public Optional<Place> getLostAndFoundPlace() {
    return lostAndFoundId != null ?
        getThing(lostAndFoundId, Place.class) : Optional.empty();
  }

  /**
   * Sets the ID for the lost and found place in this universe. This place is
   * where the contents of destroyed things go.
   *
   * @param lostAndFoundId ID of lost and found place
   */
  public void setLostAndFoundId(UUID lostAndFoundId) {
    this.lostAndFoundId = lostAndFoundId;
  }

  /**
   * Gets the ID for the guest home in this universe. This place is where
   * guests appear.
   *
   * @return ID of guest home
   */
  public UUID getGuestHomeId() {
    return guestHomeId;
  }

  /**
   * Gets the guest home place in this universe. This place is where guests
   * appear.
   *
   * @return guest home place
   */
  @JsonIgnore
  public Optional<Place> getGuestHomePlace() {
    return guestHomeId != null ?
        getThing(guestHomeId, Place.class) : Optional.empty();
  }

  /**
   * Sets the ID for the guest home place in this universe. This place is
   * where guests appear.
   *
   * @param guestHomeId ID of guest home place
   */
  public void setGuestHomeId(UUID guestHomeId) {
    this.guestHomeId = guestHomeId;
  }

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
}
