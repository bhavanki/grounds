package xyz.deszaras.grounds.combat.grapple;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.combat.Npc;

/**
 * An NPC in the Grapple combat system.
 */
public class GrappleNpc extends Npc {

  /**
   * Creates a new NPC.
   *
   * @param  name      NPC name
   * @param  statsSpec combat stats
   */
  public GrappleNpc(String name, String statsSpec) {
    super(name);
    populateCombatAttributes(Objects.requireNonNull(statsSpec));
  }

  private static final Splitter STATS_SPEC_SPLITTER =
      Splitter.on(":").limit(8).trimResults();

  private void populateCombatAttributes(String statsSpec) {
    List<String> parts = STATS_SPEC_SPLITTER.splitToList(statsSpec);
    if (parts.size() != 8) {
      throw new IllegalArgumentException("statsSpec must have eight parts");
    }

    boolean hasSkill = false;
    if (!parts.get(0).isEmpty()) {
      hasSkill = true;
      setAttr(GrappleEngine.ATTR_NAME_SKILL_4, parts.get(0));
    }
    if (!parts.get(1).isEmpty()) {
      hasSkill = true;
      setAttr(GrappleEngine.ATTR_NAME_SKILL_3, parts.get(1));
    }
    if (!parts.get(2).isEmpty()) {
      hasSkill = true;
      setAttr(GrappleEngine.ATTR_NAME_SKILL_2, parts.get(2));
    }
    if (!hasSkill) {
      throw new IllegalArgumentException("statsSpec must name at least one skill");
    }

    setAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE, Integer.parseInt(parts.get(3)));
    setAttr(GrappleEngine.ATTR_NAME_DEFENSE, Integer.parseInt(parts.get(4)));
    setAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS, Integer.parseInt(parts.get(5)));
    setAttr(GrappleEngine.ATTR_NAME_AD, Integer.parseInt(parts.get(6)));
    setAttr(GrappleEngine.ATTR_NAME_SD, Integer.parseInt(parts.get(7)));
  }
}
