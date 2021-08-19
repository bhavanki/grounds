package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;

import java.net.InetAddress;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An active participant in the game, usually a human but not
 * necessarily.
 */
public class Actor {

  /**
   * The administrative actor for the system.
   */
  public static final Actor ROOT = new Actor("root");

  /**
   * The actor behind autonomous actions in the game. No user
   * should be granted this username.
   */
  public static final Actor INTERNAL = new Actor("_internal");

  /**
   * The guest actor, shared by all guests.
   */
  public static final Actor GUEST = new Actor("guest");

  /**
   * The actor preference controlling whether ANSI escape sequences for color
   * are sent to the actor's terminal ("true") or not ("false" / default).
   */
  public static final String PREFERENCE_ANSI = "ansi";

  /**
   * The actor preference controlling whether thing IDs are provided in output
   * ("true") or not ("false" / default).
   */
  public static final String PREFERENCE_SHOW_IDS = "showIds";

  /**
   * The actor preference setting the preferred timezone.
   */
  public static final String PREFERENCE_TIMEZONE = "tz";

  private final String username;
  private final Map<String, String> preferences;

  private InetAddress mostRecentIPAddress;
  private Instant lastLoginTime;

  /**
   * Creates a new actor.
   *
   * @param username username (used for authentication)
   */
  public Actor(String username) {
    this.username = username;
    preferences = new HashMap<>();
  }

  /**
   * Gets this actor's username.
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the most recent IP address for this actor.
   *
   * @return most recent IP address
   */
  public InetAddress getMostRecentIPAddress() {
    return mostRecentIPAddress;
  }

  /**
   * Sets the most recent IP address for this actor.
   *
   * @param mostRecentIPAddress most recent IP address
   */
  public void setMostRecentIPAddress(InetAddress mostRecentIPAddress) {
    this.mostRecentIPAddress = mostRecentIPAddress;
  }

  /**
   * Gets the last login time for this actor.
   *
   * @return last login time
   */
  public Instant getLastLoginTime() {
    return lastLoginTime;
  }

  /**
   * Sets the last login time for this actor.
   *
   * @param lastLoginTime last login time
   */
  public void setLastLoginTime(Instant lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  /**
   * Gets a preference value.
   *
   * @param  name preference name
   * @return      preference value
   */
  public Optional<String> getPreference(String name) {
    return Optional.ofNullable(preferences.get(name));
  }

  /**
   * Gets all preferences.
   *
   * @return immutable map of all preferences
   */
  public Map<String, String> getPreferences() {
    return ImmutableMap.copyOf(preferences);
  }

  /**
   * Sets a preference. If the preference value is null, the preference is
   * removed.
   *
   * @param name  preference name
   * @param value preference value, or null to remove
   */
  public void setPreference(String name, String value) {
    if (value != null) {
      preferences.put(name, value);
    } else {
      preferences.remove(name);
    }
  }

  /**
   * Sets all preferences. Any current preferences are removed or replaced.
   *
   * @param preferences new preferences
   */
  public void setPreferences(Map<String, String> preferences) {
    this.preferences.clear();
    this.preferences.putAll(preferences);
  }

  public ZoneId getTimezone() {
    Optional<String> tz = getPreference(PREFERENCE_TIMEZONE);
    if (tz.isEmpty()) {
      return ZoneOffset.UTC;
    }
    try {
      return ZoneId.of(tz.get());
    } catch (DateTimeException e) {
      return ZoneOffset.UTC;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof Actor)) {
      return false;
    }

    return ((Actor) other).getUsername().equals(username);
  }

  @Override
  public int hashCode() {
    return username.hashCode();
  }
}
