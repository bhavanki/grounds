package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.TooManyStaticImports")
public class SkillTest {

  private Skill skill;

  @BeforeEach
  public void setUp() {
    skill = new Skill("Dancing", "DA", 2, true, s -> {
      s.setAd(10);
      return s;
    });
  }

  @Test
  public void testGetters() {
    assertEquals("Dancing", skill.getName());
    assertEquals("DA", skill.getAbbrev());
    assertEquals(2, skill.getActionDifficulty());
    assertTrue(skill.targetsSelf());
  }

  @Test
  public void testApplyStatsFunction() {
    Stats stats = mock(Stats.class);
    Stats newStats = skill.applyStatsFunction(stats);
    assertSame(stats, newStats);
    verify(stats).setAd(10);
  }
}
