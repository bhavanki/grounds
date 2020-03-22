package xyz.deszaras.grounds.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ActorDatabase {

  public static class ActorRecord {
    private final String username;
    private String password;
    private Set<UUID> players;

    private ActorRecord(String username, String password) {
      this.username = Objects.requireNonNull(username);
      this.password = password;
      this.players = new HashSet<>();
    }

    @JsonCreator
    private ActorRecord(
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("players") Set<UUID> players) {
      this.username = Objects.requireNonNull(username);
      this.password = password;
      this.players = players != null ? new HashSet<>(players) : new HashSet<>();
    }

    @JsonProperty
    public String getUsername() {
      return username;
    }

    @JsonProperty
    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    @JsonProperty
    public Set<UUID> getPlayers() {
      return ImmutableSet.copyOf(players);
    }

    public void addPlayer(UUID playerId) {
      players.add(playerId);
    }

    public void removePlayer(UUID playerId) {
      players.remove(playerId);
    }

    @Override
    public String toString() {
      return String.format("%s players: %s", username, players);
    }
  }

  public static final ActorDatabase INSTANCE = new ActorDatabase();

  private final Map<String, ActorRecord> actors = new HashMap<>();

  private Path path;

  private ActorDatabase() {
    path = null;
  }

  synchronized void setPath(Path path) {
    this.path = path;
  }

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper();

  public synchronized void load() throws IOException {
    if (path == null) {
      throw new IOException("Path to actor database not specified");
    }
    try {
      actors.clear();
      actors.putAll(OBJECT_MAPPER.readValue(path.toFile(),
                                            new TypeReference<Map<String, ActorRecord>>() {}));
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to load actor database", e);
    }
  }

  public synchronized void save() throws IOException {
    if (path == null) {
      throw new IOException("Path to actor database not specified");
    }
    try {
      OBJECT_MAPPER.writeValue(path.toFile(), actors);
    } catch (JsonProcessingException e) {
      throw new IOException("Failed to save actor database", e);
    }
  }

  public synchronized boolean createActorRecord(String username, String password) {
    if (actors.containsKey(username)) {
      return false;
    }
    actors.put(username, new ActorRecord(username, password));
    return true;
  }

  public synchronized Optional<ActorRecord> getActorRecord(String username) {
    return Optional.ofNullable(actors.get(username));
  }

  public synchronized boolean updateActorRecord(String username,
                                                Consumer<ActorRecord> mutation) {
    Optional<ActorRecord> actorRecord = getActorRecord(username);
    if (!actorRecord.isPresent()) {
      return false;
    }
    mutation.accept(actorRecord.get());
    return true;
  }

  public synchronized void removeActorRecord(String username) {
    actors.remove(username);
  }
}
