package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

  public Universe getUniverse(String name) {
    if (!universes.containsKey(name)) {
      throw new IllegalArgumentException("Multiverse does not contain universe " + name);
    }
    return universes.get(name);
  }

  public void putUniverse(Universe universe) {
    universes.put(universe.getName(), universe);
  }

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

}
