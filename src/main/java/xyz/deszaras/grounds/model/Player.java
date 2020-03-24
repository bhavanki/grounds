package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;

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
  public static final Player GOD = new Player("GOD", Universe.VOID, GOD_ID);

  static {
    Universe.VOID.addThing(GOD);
  }

  private Actor actor;

  public Player(String name, Universe universe) {
    super(name, universe);
  }

  private Player(String name, Universe universe, UUID id) {
    super(name, universe, id);
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
   * Builds a new player from arguments. Expected arguments: initial role.
   *
   * @param name name
   * @param universe starting universe
   * @param buildArgs build arguments
   * @return new player
   * @throws IllegalArgumentException if the number of arguments is wrong
   */
  public static Player build(String name, Universe universe, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 1, "Expected 1 build argument, got " + buildArgs.size());
    Player player = new Player(name, universe);
    universe.addRole(Role.valueOf(buildArgs.get(0).toUpperCase()), player);
    return player;
  }
}
