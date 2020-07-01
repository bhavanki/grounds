package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Message;

/**
 * A thing that represents a player in the world.
 */
public class Player extends Thing {

  /**
   * The ID of the special player GOD.
   */
  public static final UUID GOD_ID = new UUID(1L, 0L);
  /**
   * The special player GOD.
   */
  public static final Player GOD = new Player("GOD", GOD_ID);

  private final LinkedBlockingQueue<Message> messages;
  private Actor actor;

  public Player(String name) {
    super(name);
    messages = new LinkedBlockingQueue<>();
  }

  private Player(String name, UUID id) {
    super(name, id);
    messages = new LinkedBlockingQueue<>();
  }

  /**
   * Creates a new player.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Player(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);
    messages = new LinkedBlockingQueue<>();
  }

  public Optional<Place> getLocationAsPlace() throws MissingThingException {
    Optional<Thing> location = getLocation();
    if (location.isEmpty()) {
      return Optional.empty();
    }
    if (location.get() instanceof Place) {
      return Optional.of((Place) location.get());
    }
    throw new IllegalStateException("Player " + getName() + " is located at a " +
                                    "non-place: " + location.get().getId());
  }

  /**
   * Gets this player's current actor. An idling player does not have
   * any actor.
   *
   * @return current actor
   */
  @JsonIgnore
  public Optional<Actor> getCurrentActor() {
    return Optional.ofNullable(actor);
  }

  /**
   * Sets this player's current actor. Pass null to set the player to idle.
   *
   * @param actor current actor
   */
  public void setCurrentActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Sends a message to this player's current actor, as long as the sender
   * is not muted. The message is queued for delivery.
   *
   * @param message message to send
   * @throws NullPointerException if the message is null
   */
  public void sendMessage(Message message) {
    if (actor != null && !mutes(message.getSender())) {
      messages.offer(Objects.requireNonNull(message));
    }
  }

  /**
   * Gets the next message for this player's current actor from its
   * queue. This method blocks until a message is available.
   *
   * @return next available message
   * @throws InterruptedException if the wait is interrupted
   */
  @JsonIgnore
  public Message getNextMessage() throws InterruptedException {
    return messages.take();
  }

  @VisibleForTesting
  Message peekNextMessage() {
    return messages.peek();
  }

  /**
   * Clear all messages.
   */
  public void clearMessages() {
    messages.clear();
  }

  /**
   * Builds a new player from arguments. Expected arguments: initial role.
   *
   * @param name name
   * @param buildArgs build arguments
   * @return new player
   * @throws IllegalArgumentException if the number of arguments is wrong
   */
  public static Player build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 1, "Expected 1 build argument, got " + buildArgs.size());
    Player player = new Player(name);
    Universe.getCurrent().addRole(Role.valueOf(buildArgs.get(0).toUpperCase()), player);
    return player;
  }
}
