package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

  private Actor actor;

  public Player(String name) {
    super(name);
  }

  private Player(String name, UUID id) {
    super(name, id);
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
   * Sends a message to this player's current actor. The message is
   * dropped when there is no actor.
   *
   * @param message message to send
   * @return false, because players don't have listeners
   * @throws NullPointerException if the message is null
   */
  @Override
  public boolean sendMessage(Message message) {
    if (actor == null) {
      return false;
    }
    actor.sendMessage(Objects.requireNonNull(message));
    return false;
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
