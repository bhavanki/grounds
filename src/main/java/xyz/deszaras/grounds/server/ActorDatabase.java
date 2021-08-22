package xyz.deszaras.grounds.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import xyz.deszaras.grounds.security.ActorDatabasePermission;

/**
 * A simple database holding actor records. It is thread-safe.<p>
 *
 * When running with a security manager, many methods of this class are
 * guarded by {@link ActorDatabasePermission}.
 */
public class ActorDatabase {

  /**
   * The record for an actor in the actor database.
   */
  public static class ActorRecord {
    private final String username;
    private String password;
    private Set<UUID> players;
    private String mostRecentIPAddress;
    private Instant lastLoginTime;
    private Instant lockedUntil;
    private Map<String, String> preferences;

    private ActorRecord(String username, String password) {
      this.username = Objects.requireNonNull(username);
      this.password = password;
      this.players = new HashSet<>();
      this.preferences = new HashMap<>();
    }

    @JsonCreator
    private ActorRecord(
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("players") Set<UUID> players,
      @JsonProperty("mostRecentIPAddress") String mostRecentIPAddress,
      @JsonProperty("lastLoginTime") Instant lastLoginTime,
      @JsonProperty("lockedUntil") Instant lockedUntil,
      @JsonProperty("preferences") Map<String, String> preferences) {
      this.username = Objects.requireNonNull(username);
      this.password = password;
      this.players = players != null ? new HashSet<>(players) : new HashSet<>();
      this.mostRecentIPAddress = mostRecentIPAddress;
      this.lastLoginTime = lastLoginTime;
      this.lockedUntil = lockedUntil;
      this.preferences = preferences != null ?
          new HashMap<>(preferences) : new HashMap<>();
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

    @JsonProperty
    public String getMostRecentIPAddress() {
      return mostRecentIPAddress;
    }

    public void setMostRecentIPAddress(String mostRecentIPAddress) {
      this.mostRecentIPAddress = mostRecentIPAddress;
    }

    @JsonProperty
    public Instant getLastLoginTime() {
      return lastLoginTime;
    }

    public void setLastLoginTime(Instant lastLoginTime) {
      this.lastLoginTime = lastLoginTime;
    }

    @JsonProperty
    public Instant getLockedUntil() {
      return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
      this.lockedUntil = lockedUntil;
    }

    @JsonProperty
    public Map<String, String> getPreferences() {
      return ImmutableMap.copyOf(preferences);
    }

    public void setPreferences(Map<String, String> preferences) {
      this.preferences = new HashMap<>(preferences);
    }

    public void setPreference(String name, String value) {
      if (value != null) {
        preferences.put(name, value);
      } else {
        preferences.remove(name);
      }
    }

    @Override
    public String toString() {
      return String.format("%s players: %s", username, players);
    }
  }

  /**
   * Permission needed to call {@link #load()}.
   */
  public static final ActorDatabasePermission LOAD_PERMISSION =
      new ActorDatabasePermission("load");
  /**
   * Permission needed to call {@link #save()}.
   */
  public static final ActorDatabasePermission SAVE_PERMISSION =
      new ActorDatabasePermission("save");
  /**
   * Permission needed to get records from the actor database.
   */
  public static final ActorDatabasePermission READ_PERMISSION =
      new ActorDatabasePermission("read");
  /**
   * Permission needed to change records in the actor database.
   */
  public static final ActorDatabasePermission WRITE_PERMISSION =
      new ActorDatabasePermission("write");

  public static final ActorDatabase INSTANCE = new ActorDatabase();

  private final Map<String, ActorRecord> actors = new HashMap<>();

  private Path path;

  @VisibleForTesting
  ActorDatabase() {
    path = null;
  }

  @VisibleForTesting
  public synchronized void setPath(Path path) {
    this.path = path;
  }

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper();
  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  public synchronized void load() throws IOException {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(LOAD_PERMISSION);
    }
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
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(SAVE_PERMISSION);
    }
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
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(WRITE_PERMISSION);
    }
    if (actors.containsKey(username)) {
      return false;
    }
    actors.put(username, new ActorRecord(username, password));
    return true;
  }

  public synchronized Optional<ActorRecord> getActorRecord(String username) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(READ_PERMISSION);
    }
    return Optional.ofNullable(actors.get(username));
  }

  public synchronized Set<ActorRecord> getAllActorRecords() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(READ_PERMISSION);
    }
    return ImmutableSet.copyOf(actors.values());
  }

  public synchronized boolean updateActorRecord(String username,
                                                Consumer<ActorRecord> mutation) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(WRITE_PERMISSION);
    }
    Optional<ActorRecord> actorRecord = getActorRecord(username);
    if (!actorRecord.isPresent()) {
      return false;
    }
    mutation.accept(actorRecord.get());
    return true;
  }

  public synchronized void removeActorRecord(String username) {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(WRITE_PERMISSION);
    }
    actors.remove(username);
  }
}
