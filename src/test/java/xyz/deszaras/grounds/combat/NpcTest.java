package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NpcTest {

  private Npc npc;

  @BeforeEach
  public void setUp() throws Exception {
  }

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP") // lol
  @Test
  public void testPopulateCombatAttributes() {
    npc = new Npc("Robot", "a:b:c:1:2:3:4:5");

    assertEquals("a", npc.getAttr(Engine.ATTR_NAME_SKILL_4).get().getValue());
    assertEquals("b", npc.getAttr(Engine.ATTR_NAME_SKILL_3).get().getValue());
    assertEquals("c", npc.getAttr(Engine.ATTR_NAME_SKILL_2).get().getValue());

    assertEquals(1, npc.getAttr(Engine.ATTR_NAME_AP_MAX_SIZE).get().getIntValue());
    assertEquals(2, npc.getAttr(Engine.ATTR_NAME_DEFENSE).get().getIntValue());
    assertEquals(3, npc.getAttr(Engine.ATTR_NAME_MAX_WOUNDS).get().getIntValue());
    assertEquals(4, npc.getAttr(Engine.ATTR_NAME_AD).get().getIntValue());
    assertEquals(5, npc.getAttr(Engine.ATTR_NAME_SD).get().getIntValue());
  }

  @Test
  public void testRequireEightParts() {
    assertThrows(IllegalArgumentException.class,
                 () -> new Npc("Robot", "a:b:c:1:2:3:4"));
    assertThrows(IllegalArgumentException.class,
                 () -> new Npc("Robot", "a:b:c:1:2:3:4:5:6"));
  }

  @Test
  public void testRequireOneSkill() {
    assertThrows(IllegalArgumentException.class,
                 () -> new Npc("Robot", ":::1:2:3:4:5"));
  }
}
