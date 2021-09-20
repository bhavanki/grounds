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
   * Builds an NPC.
   *
   * @param  name NPC (player) name
   * @param  args additional arguments for setting up NPC
   * @return      new NPC
   * @throws IllegalArgumentException if any arguments are missing or incorrect
   */
  Npc buildNpc(String name, List<String> args);
}
