package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
   * Finds a thing in this multiverse.
   *
   * @param id spec of thing to find
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public Optional<Thing> findThing(String thingSpec) {
    if (thingSpec == null || thingSpec.trim().equals("")) {
      return Optional.empty();
    }
    String[] parts = thingSpec.split("::", 2);
    if (parts.length < 2 || !universes.containsKey(parts[0])) {
      return Optional.empty();
    }
    UUID id;
    try {
      id = UUID.fromString(parts[1]);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
    return universes.get(parts[0]).getThing(id);
  }

  /**
   * Finds a thing in this multiverse, with an expected type.
   *
   * @param id spec of thing to find
   * @param thingClass expected type of thing
   * @return thing
   * @throws IllegalArgumentException if the thing is not of the expected type
   */
  public <T extends Thing> Optional<T> findThing(String thingSpec, Class<T> thingClass) {
    try {
      return findThing(thingSpec).map(t -> thingClass.cast(t));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Thing " + thingSpec + " not of expected type " +
                                         thingClass.getName(), e);
    }
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
