package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A thing that represents a player in the world.
 */
public class Player extends Thing {

  /**
   * The god player.
   */
  public static final Player GOD = new Player(Universe.VOID, "GOD");

  static {
    Universe.VOID.addThing(GOD);
  }

  public Player(Universe universe, String name) {
    super(universe);

    setAttr(AttrNames.NAME, Objects.requireNonNull(name));
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

  @Override
  public String getName() {
    return getAttr(AttrNames.NAME).get().getValue();
  }

  public static Player build(Universe universe, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 1, "Expected 1 build argument, got " + buildArgs.size());
    return new Player(universe, buildArgs.get(0));
  }
}
