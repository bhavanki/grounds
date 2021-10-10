package xyz.deszaras.grounds.combat.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BaseStatsTest {

  private static final Skill FIGHTIN = new Skill("fightin", "ft", 2, false, null);
  private static final Skill SMACKIN = new Skill("smackin", "sm", 3, false, null);
  private static final Skill BRAWLIN = new Skill("brawlin", "br", 4, false, null);
  private static final Skill LOVIN = new Skill("lovin", "lv", 1, false, null);

  private BaseStats s;

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
  }

  @Test
  public void testImmutableStats() {
    assertEquals(2, s.getRating(BRAWLIN));
    assertEquals(3, s.getRating(SMACKIN));
    assertEquals(4, s.getRating(FIGHTIN));
    assertEquals(10, s.getApMaxSize());
    assertEquals(2, s.getDefense());
    assertEquals(3, s.getMaxWounds());
  }

  @Test
  public void testInit() {
    s.init(9, 5, 0);

    assertEquals(9, s.getAd());
    assertEquals(5, s.getSd());
    assertEquals(0, s.getWounds());
  }

  @Test
  public void testSkills() {
    Map<Skill, Integer> skills = s.getSkills();
    assertEquals(3, skills.size());
    assertEquals(2, skills.get(BRAWLIN));
    assertEquals(3, skills.get(SMACKIN));
    assertEquals(4, skills.get(FIGHTIN));

    assertThrows(IllegalArgumentException.class, () -> s.getRating(LOVIN));
  }

  @Test
  public void testAd() {
    s.init(9, 5, 0);

    s.addAd(-2);
    assertEquals(7, s.getAd());

    s.addAd(100);
    assertEquals(10, s.getAd());

    s.addAd(-100);
    assertEquals(0, s.getAd());

    s.setAd(9);
    assertEquals(9, s.getAd());

    s.setAd(100);
    assertEquals(10, s.getAd());

    s.setAd(-100);
    assertEquals(0, s.getAd());
  }

  @Test
  public void testSd() {
    s.init(9, 5, 0);

    s.addSd(-2);
    assertEquals(3, s.getSd());

    s.addSd(3);
    assertEquals(6, s.getSd());

    s.addSd(-6);
    assertEquals(0, s.getSd());

    s.setSd(5);
    assertEquals(5, s.getSd());
  }

  @Test
  public void testWounds() {
    s.init(9, 5, 0);
    assertFalse(s.isOut());

    s.wound();
    assertEquals(1, s.getWounds());
    assertFalse(s.isOut());

    s.wound(2);
    assertEquals(3, s.getWounds());
    assertTrue(s.isOut());

    s.wound(100);
    assertEquals(3, s.getWounds());
    assertTrue(s.isOut());

    s.wound(-1);
    assertEquals(2, s.getWounds());
    assertFalse(s.isOut());

    s.wound(-100);
    assertEquals(0, s.getWounds());
    assertFalse(s.isOut());
  }

  @Test
  public void testProto() {
    // need to use real skills
    s = BaseStats.builder()
        .skill(Skills.ENDURANCE, 2)
        .skill(Skills.COURAGE, 3)
        .skill(Skills.ACCURACY, 4)
        .apMaxSize(10)
        .defense(2)
        .maxWounds(3)
        .build();
    s.init(9, 5, 1);
    s.useSkill(Skills.ACCURACY);

    ProtoModel.Stats ps = s.toProto();
    assertEquals(0, ps.getStatsDecoratorsCount());
    ProtoModel.BaseStats pbs = ps.getBaseStats();

    assertEquals(3, pbs.getSkillsCount());
    Map<String, Integer> skillsMap = pbs.getSkillsMap();
    assertEquals(4, skillsMap.get(Skills.ACCURACY.getName()));
    assertEquals(3, skillsMap.get(Skills.COURAGE.getName()));
    assertEquals(2, skillsMap.get(Skills.ENDURANCE.getName()));

    assertEquals(3, pbs.getSkillUsesCount());
    Map<String, Boolean> skillUsesMap = pbs.getSkillUsesMap();
    assertEquals(true, skillUsesMap.get(Skills.ACCURACY.getName()));
    assertEquals(false, skillUsesMap.get(Skills.COURAGE.getName()));
    assertEquals(false, skillUsesMap.get(Skills.ENDURANCE.getName()));

    assertEquals(10, pbs.getApMaxSize());
    assertEquals(2, pbs.getDefense());
    assertEquals(3, pbs.getMaxWounds());
    assertEquals(9, pbs.getAd());
    assertEquals(5, pbs.getSd());
    assertEquals(1, pbs.getWounds());

    BaseStats s2 = BaseStats.fromProto(pbs);

    assertEquals(4, s2.getRating(Skills.ACCURACY));
    assertEquals(3, s2.getRating(Skills.COURAGE));
    assertEquals(2, s2.getRating(Skills.ENDURANCE));

    assertEquals(true, s2.isUsed(Skills.ACCURACY));
    assertEquals(false, s2.isUsed(Skills.COURAGE));
    assertEquals(false, s2.isUsed(Skills.ENDURANCE));

    assertEquals(10, s2.getApMaxSize());
    assertEquals(2, s2.getDefense());
    assertEquals(3, s2.getMaxWounds());
    assertEquals(9, s2.getAd());
    assertEquals(5, s2.getSd());
    assertEquals(1, s2.getWounds());
  }
}
