package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A set of universes. Each instance of the game is a single
 * multiverse.
 */
public class Multiverse {

  private Multiverse() {
  }

  public static final Multiverse MULTIVERSE = new Multiverse();

  private final Map<String, Universe> universes = new HashMap<>();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void load(File f) throws IOException {
    try {
      MULTIVERSE.universes.clear();
      MULTIVERSE.universes.putAll(OBJECT_MAPPER.readValue(f, new TypeReference<Map<String, Universe>>() {}));
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to load multiverse", e);
    }
  }

  public static void save(File f) throws IOException {
    try {
      OBJECT_MAPPER.writeValue(f, MULTIVERSE.universes);
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to save multiverse", e);
    }
  }

  public boolean hasUniverse(String name) {
    return universes.containsKey(name);
  }

  public Universe getUniverse(String name) {
    if (!universes.containsKey(name)) {
      throw new IllegalArgumentException("Multiverse does not contain universe " + name);
    }
    return universes.get(name);
  }

  public void putUniverse(Universe universe) {
    universes.put(universe.getName(), universe);
  }

  /**
   * Finds a thing in this multiverse by its ID.
   *
   * @param idString ID of thing to find
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public Optional<Thing> findThing(String idString) {
    if (idString == null || idString.trim().equals("")) {
      return Optional.empty();
    }
    try {
      return findThing(UUID.fromString(idString));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Finds a thing in this multiverse by its ID.
   *
   * @param id ID of thing to find
   * @return thing
   */
  public Optional<Thing> findThing(UUID id) {
    if (id == null) {
      return Optional.empty();
    }
    for (Universe universe : universes.values()) {
      Optional<Thing> foundThing = universe.getThing(id);
      if (foundThing.isPresent()) {
        return foundThing;
      }
    }
    return Optional.empty();
  }

  /**
   * Finds a thing in this multiverse, with an expected type.
   *
   * @param idString ID of thing to find
   * @param thingClass expected type of thing
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public <T extends Thing> Optional<T> findThing(String idString, Class<T> thingClass) {
    try {
      return findThing(idString).map(t -> thingClass.cast(t));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Thing " + idString + " not of expected type " +
                                         thingClass.getName(), e);
    }
  }

  /**
   * Finds a thing in this multiverse, with an expected type.
   *
   * @param id ID of thing to find
   * @param thingClass expected type of thing
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public <T extends Thing> Optional<T> findThing(UUID id, Class<T> thingClass) {
    try {
      return findThing(id).map(t -> thingClass.cast(t));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Thing " + id.toString() + " not of expected type " +
                                         thingClass.getName(), e);
    }
  }

  /**
   * Finds a thing in this multiverse by name, with an expected type. If
   * multiple things share the name, an arbitrary one is returned.
   *
   * @param name name of thing to find
   * @param thingClass expected type of thing
   * @return thing
   */
  public <T extends Thing> Optional<T> findThingByName(String name, Class<T> thingClass) {
    if (name == null) {
      return Optional.empty();
    }
    // This is currently quite expensive
    for (Universe universe : universes.values()) {
      Optional<T> foundThing = universe.getThingByName(name, thingClass);
      if (foundThing.isPresent()) {
        return foundThing;
      }
    }
    return Optional.empty();
  }

  public Collection<Link> findLinks(Place place) {
    // this is dreadfully inefficient
    return universes.values().stream()
        .flatMap(u -> u.getThings(Link.class).stream())
        .filter(l -> l.linksTo(place))
        .collect(Collectors.toUnmodifiableSet());
  }

  public Optional<Link> findLink(Place source, Place destination) {
    // this is dreadfully inefficient
    return universes.values().stream()
        .flatMap(u -> u.getThings(Link.class).stream())
        .filter(l -> l.linksTo(source) && l.linksTo(destination))
        .findFirst();
  }
}
