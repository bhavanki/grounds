package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class TeamTest {

  private Player player1;
  private Player player2;
  private Player player3;
  private Team team;

  @BeforeEach
  public void setUp() {
    player1 = new Player("player1");
    player1.setAttr(Engine.ATTR_NAME_SKILL_4, Skills.ACCURACY.getName());
    player1.setAttr(Engine.ATTR_NAME_SKILL_3, Skills.COURAGE.getName());
    player1.setAttr(Engine.ATTR_NAME_SKILL_2, Skills.ENDURANCE.getName());
    player1.setAttr(Engine.ATTR_NAME_AP_MAX_SIZE, 10);
    player1.setAttr(Engine.ATTR_NAME_DEFENSE, 3);
    player1.setAttr(Engine.ATTR_NAME_MAX_WOUNDS, 4);
    player1.setAttr(Engine.ATTR_NAME_AD, 1);
    player1.setAttr(Engine.ATTR_NAME_SD, 2);
    player2 = new Player("player2");
    player2.setAttr(Engine.ATTR_NAME_SKILL_4, Skills.INTIMIDATION.getName());
    player2.setAttr(Engine.ATTR_NAME_SKILL_3, Skills.LEADERSHIP.getName());
    player2.setAttr(Engine.ATTR_NAME_SKILL_2, Skills.MEDICAL.getName());
    player2.setAttr(Engine.ATTR_NAME_AP_MAX_SIZE, 12);
    player2.setAttr(Engine.ATTR_NAME_DEFENSE, 2);
    player2.setAttr(Engine.ATTR_NAME_MAX_WOUNDS, 4);
    player2.setAttr(Engine.ATTR_NAME_AD, 3);
    player2.setAttr(Engine.ATTR_NAME_SD, 4);
    player3 = new Player("player3");
    team = Team.builder("teama")
        .member(player1)
        .member(player2)
        .build();
  }

  @Test
  public void testName() {
    assertEquals("teama", team.getName());
  }

  @Test
  public void testMembership() {
    assertTrue(team.isMember(player1));
    assertFalse(team.isMember(player3));

    assertEquals(player1, team.getMemberByName(player1.getName()).get());
    assertTrue(team.getMemberByName(player3.getName()).isEmpty());

    assertEquals(Set.of(player1, player2), team.getMembers());
  }

  @Test
  public void testGetMemberStats() {
    Stats stats = team.getMemberStats(player1);
    assertEquals(4, stats.getRating(Skills.ACCURACY));
    assertEquals(3, stats.getRating(Skills.COURAGE));
    assertEquals(2, stats.getRating(Skills.ENDURANCE));
    assertEquals(10, stats.getApMaxSize());
    assertEquals(3, stats.getDefense());
    assertEquals(4, stats.getMaxWounds());
    assertEquals(1, stats.getAd());
    assertEquals(2, stats.getSd());

    assertThrows(IllegalArgumentException.class,
                 () -> team.getMemberStats(player3));
  }

  @Test
  public void testSetMemberStats() {
    assertEquals(0, team.getMemberStats(player1).getManeuverBonus());
    Stats stats = team.getMemberStats(player1);
    Stats newStats = new StatsDecorator(stats) {
      @Override
      public int getManeuverBonus() {
        return 1;
      }
    };

    team.setMemberStats(player1, newStats);

    assertEquals(1, team.getMemberStats(player1).getManeuverBonus());

    assertThrows(IllegalArgumentException.class,
                 () -> team.setMemberStats(player3, newStats));
  }

  @Test
  public void testIsOut() {
    assertFalse(team.isOut());

    team.getMemberStats(player1).wound(4);
    assertFalse(team.isOut());

    team.getMemberStats(player2).wound(4);
    assertTrue(team.isOut());
  }

  @Test
  public void testBuildSuccessRemovePlayer() {
    team = Team.builder("teama")
      .member(player1)
      .member(player2)
      .removeMember(player1)
      .build();

    assertTrue(team.isMember(player2));
    assertFalse(team.isMember(player1));
  }

  @Test
  public void testBuildFailureRepeatedPlayer() {
    Team.Builder tb = Team.builder("teama")
      .member(player1)
      .member(player2);

    assertThrows(IllegalArgumentException.class,
                 () -> tb.member(player1));
  }
}
