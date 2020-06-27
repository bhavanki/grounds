package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import xyz.deszaras.grounds.model.Player;

/**
 * An active participant in the game, usually a human but not
 * necessarily. An actor should be associated with the player that
 * they are currently "driving".
 */
public class Actor {

  /**
   * The administrative actor for the system. No user should be
   * granted this username.
   */
  public static final Actor ROOT = new Actor("root");

  public static final String PREFERENCE_ANSI = "ansi";

  private final String username;
  private final Map<String, String> preferences;

  private Player currentPlayer;
  private InetAddress mostRecentIPAddress;

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
   * Gets the current player for this actor.
   *
   * @return current player
   */
  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Sets the current player for this actor.
   *
   * @return current player
   */
  public void setCurrentPlayer(Player player) {
    currentPlayer = player;
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
