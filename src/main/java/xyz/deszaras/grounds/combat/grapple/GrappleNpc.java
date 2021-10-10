package xyz.deszaras.grounds.combat.grapple;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.deszaras.grounds.combat.Npc;
import xyz.deszaras.grounds.model.Attr;

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

  public GrappleNpc(String name, Stats stats) {
    super(name);
    populateCombatAttributes(Objects.requireNonNull(stats));
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

  private void populateCombatAttributes(Stats stats) {
    Stats bs = stats;
    while (bs instanceof StatsDecorator) {
      bs = ((StatsDecorator) bs).getDelegate();
    }
    boolean hasSkill = false;
    for (Map.Entry<Skill, Integer> e : bs.getSkills().entrySet()) {
      String skillName = e.getKey().getName();
      switch (e.getValue()) {
        case 4:
          setAttr(GrappleEngine.ATTR_NAME_SKILL_4, skillName);
          hasSkill = true;
          break;
        case 3:
          setAttr(GrappleEngine.ATTR_NAME_SKILL_3, skillName);
          hasSkill = true;
          break;
        case 2:
          setAttr(GrappleEngine.ATTR_NAME_SKILL_2, skillName);
          hasSkill = true;
          break;
      }
    }
    if (!hasSkill) {
      throw new IllegalArgumentException("stats must have at least one skill");
    }

    setAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE, bs.getApMaxSize());
    setAttr(GrappleEngine.ATTR_NAME_DEFENSE, bs.getDefense());
    setAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS, bs.getMaxWounds());
    setAttr(GrappleEngine.ATTR_NAME_AD, bs.getAd());
    setAttr(GrappleEngine.ATTR_NAME_SD, bs.getSd());
  }

  private static final String STATS_SPEC_FORMAT = "%s:%s:%s:%d:%d:%d:%d:%d";

  public ProtoModel.Npc toProto() {
    return ProtoModel.Npc.newBuilder()
        .setName(getName())
        .setStatsSpec(generateStatsSpec())
        .build();
  }

  private String generateStatsSpec() {
    String skill4 = getAttr(GrappleEngine.ATTR_NAME_SKILL_4)
        .map(Attr::getValue)
        .orElse("");
    String skill3 = getAttr(GrappleEngine.ATTR_NAME_SKILL_3)
        .map(Attr::getValue)
        .orElse("");
    String skill2 = getAttr(GrappleEngine.ATTR_NAME_SKILL_2)
        .map(Attr::getValue)
        .orElse("");
    return String.format(STATS_SPEC_FORMAT, skill4, skill3, skill2,
                         getAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE).get().getIntValue(),
                         getAttr(GrappleEngine.ATTR_NAME_DEFENSE).get().getIntValue(),
                         getAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS).get().getIntValue(),
                         getAttr(GrappleEngine.ATTR_NAME_AD).get().getIntValue(),
                         getAttr(GrappleEngine.ATTR_NAME_SD).get().getIntValue());
  }

  public static GrappleNpc fromProto(ProtoModel.Npc proto) {
    return new GrappleNpc(proto.getName(), proto.getStatsSpec());
  }
}
