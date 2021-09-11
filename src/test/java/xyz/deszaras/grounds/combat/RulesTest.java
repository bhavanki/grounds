package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.combat.Rules.CatchBreathInput;
import xyz.deszaras.grounds.combat.Rules.CatchBreathOutput;
import xyz.deszaras.grounds.combat.Rules.ManeuverInput;
import xyz.deszaras.grounds.combat.Rules.ManeuverOutput;
import xyz.deszaras.grounds.combat.Rules.SkillActionInput;
import xyz.deszaras.grounds.combat.Rules.SkillActionOutput;
import xyz.deszaras.grounds.combat.Rules.StrikeInput;
import xyz.deszaras.grounds.combat.Rules.StrikeOutput;

@SuppressWarnings("PMD.TooManyStaticImports")
public class RulesTest {

  private static final Function<Stats, Stats> DROP_DEF =
    s -> new StatsDecorator(s) {
      @Override
      public int getDefense() {
        return delegate.getDefense() - 1;
      }
    };

  private static final Function<Stats, Stats> RAISE_DEF =
    s -> new StatsDecorator(s) {
      @Override
      public int getDefense() {
        return delegate.getDefense() + 1;
      }
    };

  private static final Skill FIGHTIN = new Skill("fightin", "ft", 2, true, RAISE_DEF);
  private static final Skill SMACKIN = new Skill("smackin", "sm", 3, false, null);
  private static final Skill BRAWLIN = new Skill("brawlin", "br", 4, false, DROP_DEF);

  private BaseStats s;
  private BaseStats ds;
  private Rules r;

  @BeforeEach
  public void setUp() {
    s = BaseStats.builder()
        .skill(BRAWLIN, 2)
        .skill(SMACKIN, 3)
        .skill(FIGHTIN, 4)
        .apMaxSize(10)
        .defense(2)
        .maxWounds(3)
        .build();
    s.init(9, 5, 0);

    ds = BaseStats.builder()
        .skill(BRAWLIN, 2)
        .skill(SMACKIN, 3)
        .skill(FIGHTIN, 4)
        .apMaxSize(10)
        .defense(2)
        .maxWounds(3)
        .build();
    ds.init(9, 5, 0);
  }

  @Test
  public void testManeuverSuccess() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 5, 6, 1 };
      }
    };

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, BRAWLIN, 3));

    assertTrue(o.success);
    assertEquals(3, o.adSpent);
    assertEquals(2, o.sdEarned);
    assertEquals(7, o.newSd);

    assertTrue(s.isUsed(BRAWLIN));
  }

  @Test
  public void testManeuverFailure() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1, 1 };
      }
    };

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, BRAWLIN, 3));

    assertFalse(o.success);
    assertEquals(0, o.adSpent);
    assertEquals(0, o.sdEarned);
    assertEquals(0, o.sdFromSkillUse);
    assertEquals(5, o.newSd);

    assertTrue(s.isUsed(BRAWLIN));
  }

  @Test
  public void testManeuverSuccessWithSkillUse() {
    s.useSkill(FIGHTIN);
    s.useSkill(SMACKIN);
    s.useSkill(BRAWLIN);
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 5, 6, 1 };
      }
    };

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, BRAWLIN, 3));

    assertTrue(o.success);
    assertEquals(3, o.adSpent);
    assertEquals(2, o.sdEarned);
    assertEquals(2, o.sdFromSkillUse);
    assertEquals(9, o.newSd);

    assertFalse(s.isUsed(FIGHTIN));
    assertFalse(s.isUsed(SMACKIN));
    assertTrue(s.isUsed(BRAWLIN));
  }

  @Test
  public void testManeuverSuccessWithDisabledSkillUse() {
    s = BaseStats.builder().npc()
        .skill(BRAWLIN, 2)
        .skill(SMACKIN, 3)
        .apMaxSize(10)
        .defense(2)
        .maxWounds(3)
        .build();
    s.init(9, 5, 0);

    s.useSkill(SMACKIN);
    s.useSkill(BRAWLIN);
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 5, 6, 1 };
      }
    };

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, BRAWLIN, 3));

    assertTrue(o.success);
    assertEquals(3, o.adSpent);
    assertEquals(2, o.sdEarned);
    assertEquals(0, o.sdFromSkillUse);
    assertEquals(7, o.newSd);

    assertTrue(s.isUsed(SMACKIN));
    assertTrue(s.isUsed(BRAWLIN));
  }

  @Test
  public void testManeuverFailureWithSkillUse() {
    s.useSkill(FIGHTIN);
    s.useSkill(SMACKIN);
    s.useSkill(BRAWLIN);
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1, 1 };
      }
    };

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, BRAWLIN, 3));

    assertFalse(o.success);
    assertEquals(0, o.adSpent);
    assertEquals(0, o.sdEarned);
    assertEquals(2, o.sdFromSkillUse);
    assertEquals(7, o.newSd);

    assertFalse(s.isUsed(FIGHTIN));
    assertFalse(s.isUsed(SMACKIN));
    assertTrue(s.isUsed(BRAWLIN));
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

    ManeuverOutput o = r.maneuver(new ManeuverInput(s, FIGHTIN, 2));

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

    StrikeOutput o = r.strike(new StrikeInput(s, 3, s));

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

    StrikeOutput o = r.strike(new StrikeInput(s, 3, s));

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

    StrikeOutput o = r.strike(new StrikeInput(s, 6, s));

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
                 () -> r.strike(new StrikeInput(s, 2, s)));

    assertEquals(1, s.getSd());
  }

  @Test
  public void testStrikeNotEnoughStrikeDice2() {
    r = new Rules();

    assertThrows(IllegalArgumentException.class,
                 () -> r.strike(new StrikeInput(s, 0, s)));

    assertEquals(5, s.getSd());
  }

  @Test
  public void testStrikeTooManyStrikeDice() {
    r = new Rules();
    s.setSd(7);

    assertThrows(IllegalArgumentException.class,
                 () -> r.strike(new StrikeInput(s, 7, s)));

    assertEquals(7, s.getSd());
  }

  @Test
  public void testSkillActionSuccessSelf() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 5 };
      }
    };

    SkillActionOutput o = r.skill(new SkillActionInput(s, 2, FIGHTIN, null));

    assertTrue(o.success);
    assertEquals(2, o.sdSpent);
    assertEquals(3, s.getSd());
    assertEquals(4, o.numSuccs);

    assertEquals(3, o.newStats.getDefense());
    assertNull(o.newDStats);
  }

  @Test
  public void testSkillActionSuccessOther() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 5, 5, 5, 5 };
      }
    };

    SkillActionOutput o = r.skill(new SkillActionInput(s, 2, BRAWLIN, ds));

    assertTrue(o.success);
    assertEquals(2, o.sdSpent);
    assertEquals(3, s.getSd());
    assertEquals(4, o.numSuccs);

    assertNull(o.newStats);
    assertEquals(1, o.newDStats.getDefense());
  }

  @Test
  public void testSkillActionFailure() {
    r = new Rules() {
      @Override
      public int[] roll(int n) {
        return new int[] { 1, 1, 1, 1 };
      }
    };

    SkillActionOutput o = r.skill(new SkillActionInput(s, 2, BRAWLIN, ds));

    assertFalse(o.success);
    assertEquals(0, o.sdSpent);
    assertEquals(5, s.getSd());
    assertEquals(0, o.numSuccs);

    assertNull(o.newStats);
    assertNull(o.newDStats);
  }

  @Test
  public void testSkillActionNotEnoughStrikeDice() {
    r = new Rules();
    s.setSd(1);

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(new SkillActionInput(s, 2, BRAWLIN, ds)));

    assertEquals(1, s.getSd());
  }

  @Test
  public void testAchieveNotEnoughStrikeDice2() {
    r = new Rules();

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(new SkillActionInput(s, -1, BRAWLIN, ds)));

    assertEquals(5, s.getSd());
  }

  @Test
  public void testAchieveTooManyStrikeDice() {
    r = new Rules();
    s.setSd(7);

    assertThrows(IllegalArgumentException.class,
                 () -> r.skill(new SkillActionInput(s, 7, BRAWLIN, ds)));

    assertEquals(7, s.getSd());
  }

  @Test
  public void testCatchBreath() {
    r = new Rules();
    s.setAd(5);

    CatchBreathOutput o = r.catchBreath(new CatchBreathInput(s));

    assertEquals(3, o.adEarned);
    assertEquals(8, s.getAd());
  }

  @Test
  public void testSuccs() {
    assertEquals(4, Rules.succs(new int[] {1, 2, 3, 4, 5, 6}));
    assertEquals(0, Rules.succs(new int[] {1, 2, 1, 2, 1, 2}));
    assertEquals(6, Rules.succs(new int[] {6, 5, 4, 3, 6, 5}));
    assertEquals(0, Rules.succs(new int[0]));
  }
}
