package xyz.deszaras.grounds.combat.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrappleNpcTest {

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String STATS_SPEC = "a:b:c:6:2:3:4:5";

  private static final Skill SKILL_A = new Skill("a", "a", 1, false, null);
  private static final Skill SKILL_B = new Skill("b", "b", 1, false, null);
  private static final Skill SKILL_C = new Skill("c", "c", 1, false, null);

  private GrappleNpc npc;

  @BeforeEach
  public void setUp() {
    npc = new GrappleNpc("Robot", STATS_SPEC);
  }

  @Test
  public void testPopulateCombatAttributesFromSpec() {
    verifyCombatAttributes(npc);
  }

  @Test
  public void testPopulateCombatAttributesFromStats() {
    BaseStats stats = BaseStats.builder()
        .skill(SKILL_A, 4)
        .skill(SKILL_B, 3)
        .skill(SKILL_C, 2)
        .apMaxSize(6)
        .defense(2)
        .maxWounds(3)
        .build();
    stats.init(4, 5, 1);
    npc = new GrappleNpc("Robot", stats);
    verifyCombatAttributes(npc);
  }

  private void verifyCombatAttributes(GrappleNpc npc) {
    assertEquals("a", npc.getAttr(GrappleEngine.ATTR_NAME_SKILL_4).get().getValue());
    assertEquals("b", npc.getAttr(GrappleEngine.ATTR_NAME_SKILL_3).get().getValue());
    assertEquals("c", npc.getAttr(GrappleEngine.ATTR_NAME_SKILL_2).get().getValue());

    assertEquals(6, npc.getAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE).get().getIntValue());
    assertEquals(2, npc.getAttr(GrappleEngine.ATTR_NAME_DEFENSE).get().getIntValue());
    assertEquals(3, npc.getAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS).get().getIntValue());
    assertEquals(4, npc.getAttr(GrappleEngine.ATTR_NAME_AD).get().getIntValue());
    assertEquals(5, npc.getAttr(GrappleEngine.ATTR_NAME_SD).get().getIntValue());
  }

  @Test
  public void testRequireEightParts() {
    assertThrows(IllegalArgumentException.class,
                 () -> new GrappleNpc("Robot", "a:b:c:6:2:3:4"));
    assertThrows(IllegalArgumentException.class,
                 () -> new GrappleNpc("Robot", "a:b:c:6:2:3:4:5:6"));
  }

  @Test
  public void testRequireOneSkill() {
    assertThrows(IllegalArgumentException.class,
                 () -> new GrappleNpc("Robot", ":::6:2:3:4:5"));
  }

  @Test
  public void testProto() {
    ProtoModel.Npc pn = npc.toProto();

    assertEquals("Robot", pn.getName());
    assertEquals(STATS_SPEC, pn.getStatsSpec());

    GrappleNpc n2 = GrappleNpc.fromProto(pn);

    assertEquals("Robot", n2.getName());
    verifyCombatAttributes(n2);
  }
}
