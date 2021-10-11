package xyz.deszaras.grounds.combat;

import java.util.List;

/**
 * A combat system.
 */
public interface System {

  /**
   * Gets a team builder.
   *
   * @param  name team name
   * @return      team builder
   */
  Team.Builder getTeamBuilder(String name);

  /**
   * Gets an engine builder.
   *
   * @return engine builder
   */
  Engine.Builder getEngineBuilder();

  /**
   * Constructs an engine from saved state. Optional operation; an
   * implementation which does not support saved state should throw
   * {@code UnsupportedOperationException}.
   *
   * @param state state
   * @return engine
   * @throws IllegalArgumentException if the state is invalid
   * @throws UnsupportedOperationException if this sytem does not save state
   */
  Engine restore(byte[] state);

  /**
   * Builds an NPC.
   *
   * @param  name NPC (player) name
   * @param  args additional arguments for setting up NPC
   * @return      new NPC
   * @throws IllegalArgumentException if any arguments are missing or incorrect
   */
  Npc buildNpc(String name, List<String> args);
}
