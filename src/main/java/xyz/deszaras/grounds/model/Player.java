package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Role;

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
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Player(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents) {
    super(id, attrs, contents);
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
