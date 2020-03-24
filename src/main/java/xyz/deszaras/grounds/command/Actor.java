package xyz.deszaras.grounds.command;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Objects;
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

  private final String username;
  private final LinkedBlockingQueue<String> messages;

  private Player currentPlayer;

  /**
   * Creates a new actor.
   *
   * @param username username (used for authentication)
   */
  public Actor(String username) {
    this.username = username;
    messages = new LinkedBlockingQueue<>();
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
   * Sends a message to this actor. The message is queued for
   * delivery.
   *
   * @param message message to send
   * @throws NullPointerException if the message is null
   */
  public void sendMessage(String message) {
    messages.offer(Objects.requireNonNull(message));
  }

  /**
   * Gets the next message for this actor from its queue. This method
   * blocks until a message is available.
   *
   * @return next available message
   * @throws InterruptedException if the wait is interrupted
   */
  public String getNextMessage() throws InterruptedException {
    return messages.take();
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
