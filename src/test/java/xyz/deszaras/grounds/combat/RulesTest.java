package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.combat.Rules.CatchBreathOutcome;
import xyz.deszaras.grounds.combat.Rules.ManeuverOutcome;
import xyz.deszaras.grounds.combat.Rules.SkillActionOutcome;
import xyz.deszaras.grounds.combat.Rules.StrikeOutcome;

public class RulesTest {

  private static final Skill FIGHTIN = new Skill("fightin", 2);
  private static final Skill SMACKIN = new Skill("smackin", 3);
  private static final Skill BRAWLIN = new Skill("brawlin", 4);

  private Stats s;
  private Rules r;
  private Skill sk;

  @BeforeEach
  public void setUp() {
    s = Stats.builder()
        .skill(BRAWLIN, 2)
        .skill(SMACKIN, 3)
        .skill(FIGHTIN, 4)
        .apMaxSize(10)
        .defense(2)
        .maxWounds(3)
        .build();
    s.init(9, 5, 0);
  }

  @Test
  public void testManeuverSuccess() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 5, 6, 1 };
      }
    };

    ManeuverOutcome o = r.maneuver(s, BRAWLIN, 3);

    assertTrue(o.success);
    assertEquals(3, o.adSpent);
    assertEquals(2, o.sdEarned);
  }

  @Test
  public void testManeuverFailure() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1, 1 };
      }
    };

    ManeuverOutcome o = r.maneuver(s, BRAWLIN, 3);

    assertFalse(o.success);
    assertEquals(0, o.adSpent);
    assertEquals(0, o.sdEarned);
  }

  @Test
  public void testManeuverMaxOutSD() {
    s.init(9, 1, 0);

    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 5, 5, 5 };
      }
    };

    ManeuverOutcome o = r.maneuver(s, FIGHTIN, 2);

    assertTrue(o.success);
    assertEquals(2, o.adSpent);
    assertEquals(5, o.sdEarned);
    assertEquals(6, s.getSd());
  }

  @Test
  public void testStrikeSuccess() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 1 };
      }
    };

    StrikeOutcome o = r.strike(s, 3, s);

    assertTrue(o.success);
    assertEquals(3, o.sdSpent);
    assertEquals(2, s.getSd());
    assertEquals(1, s.getWounds());
    assertEquals(3, o.numSuccs);
  }

  @Test
  public void testStrikeFailure() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1 };
      }
    };

    StrikeOutcome o = r.strike(s, 3, s);

    assertFalse(o.success);
    assertEquals(0, o.sdSpent);
    assertEquals(5, s.getSd());
    assertEquals(0, s.getWounds());
    assertEquals(0, o.numSuccs);
  }

  @Test
  public void testStrikeSuccessMultiWound() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 5, 5, 5 };
      }
    };
    s.setSd(7);

    StrikeOutcome o = r.strike(s, 6, s);

    assertTrue(o.success);
    assertEquals(6, o.sdSpent);
    assertEquals(1, s.getSd());
    assertEquals(3, s.getWounds());
    assertEquals(6, o.numSuccs);
  }

  @Test
  public void testStrikeNotEnoughStrikeDice() {
    r = new Rules();
    s.setSd(1);

    assertThrows(IllegalArgumentException.class,
                 () -> r.strike(s, 2, s));

    assertEquals(1, s.getSd());
  }

  @Test
  public void testStrikeNotEnoughStrikeDice2() {
    r = new Rules();

    assertThrows(IllegalArgumentException.class,
                 () -> r.strike(s, 0, s));

    assertEquals(5, s.getSd());
  }

  @Test
  public void testStrikeTooManyStrikeDice() {
    r = new Rules();
    s.setSd(7);

    assertThrows(IllegalArgumentException.class,
                 () -> r.strike(s, 7, s));

    assertEquals(7, s.getSd());
  }

  @Test
  public void testSkillActionSuccess() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 5 };
      }
    };

    SkillActionOutcome o = r.skill(s, 2, BRAWLIN);

    assertTrue(o.success);
    assertEquals(2, o.sdSpent);
    assertEquals(3, s.getSd());
    assertEquals(4, o.numSuccs);
  }

  @Test
  public void testSkillActionFailure() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1 };
      }
    };

    SkillActionOutcome o = r.skill(s, 2, BRAWLIN);

    assertFalse(o.success);
    assertEquals(0, o.sdSpent);
    assertEquals(5, s.getSd());
    assertEquals(0, o.numSuccs);
  }

  @Test
  public void testSkillActionNotEnoughStrikeDice() {
    r = new Rules();
    s.setSd(1);

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(s, 2, BRAWLIN));

    assertEquals(1, s.getSd());
  }

  @Test
  public void testAchieveNotEnoughStrikeDice2() {
    r = new Rules();

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(s, -1, BRAWLIN));

    assertEquals(5, s.getSd());
  }

  @Test
  public void testAchieveTooManyStrikeDice() {
    r = new Rules();
    s.setSd(7);

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(s, 7, BRAWLIN));

    assertEquals(7, s.getSd());
  }

  @Test
  public void testCatchBreath() {
    r = new Rules();
    s.setAd(5);

    CatchBreathOutcome o = r.catchBreath(s);

    assertEquals(2, o.adEarned);
    assertEquals(7, s.getAd());
  }

}
