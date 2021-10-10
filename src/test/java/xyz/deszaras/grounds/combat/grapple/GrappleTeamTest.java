package xyz.deszaras.grounds.combat.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class GrappleTeamTest {

  private static final String NPC_STATS_SPEC = "speed:spirit:strategy:13:3:3:5:6";

  private Universe testUniverse;
  private Player player1;
  private Player player2;
  private GrappleNpc npc1;
  private Player player3;
  private GrappleTeam team;

  @BeforeEach
  public void setUp() {
    testUniverse = new Universe("test");
    Universe.setCurrent(testUniverse);

    player1 = new Player("player1");
    player1.setAttr(GrappleEngine.ATTR_NAME_SKILL_4, Skills.ACCURACY.getName());
    player1.setAttr(GrappleEngine.ATTR_NAME_SKILL_3, Skills.COURAGE.getName());
    player1.setAttr(GrappleEngine.ATTR_NAME_SKILL_2, Skills.ENDURANCE.getName());
    player1.setAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE, 10);
    player1.setAttr(GrappleEngine.ATTR_NAME_DEFENSE, 3);
    player1.setAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS, 4);
    player1.setAttr(GrappleEngine.ATTR_NAME_AD, 1);
    player1.setAttr(GrappleEngine.ATTR_NAME_SD, 2);
    testUniverse.addThing(player1);

    player2 = new Player("player2");
    player2.setAttr(GrappleEngine.ATTR_NAME_SKILL_4, Skills.INTIMIDATION.getName());
    player2.setAttr(GrappleEngine.ATTR_NAME_SKILL_3, Skills.LEADERSHIP.getName());
    player2.setAttr(GrappleEngine.ATTR_NAME_SKILL_2, Skills.MEDICAL.getName());
    player2.setAttr(GrappleEngine.ATTR_NAME_AP_MAX_SIZE, 12);
    player2.setAttr(GrappleEngine.ATTR_NAME_DEFENSE, 2);
    player2.setAttr(GrappleEngine.ATTR_NAME_MAX_WOUNDS, 4);
    player2.setAttr(GrappleEngine.ATTR_NAME_AD, 3);
    player2.setAttr(GrappleEngine.ATTR_NAME_SD, 4);
    testUniverse.addThing(player2);

    npc1 = new GrappleNpc("npc1", NPC_STATS_SPEC);

    player3 = new Player("player3");
    testUniverse.addThing(player3);

    team = GrappleTeam.builder("teama")
        .member(player1)
        .member(player2)
        .member(npc1)
        .build();
  }

  @Test
  public void testName() {
    assertEquals("teama", team.getName());
  }

  @Test
  public void testMembership() {
    assertTrue(team.isMember(player1));
    assertTrue(team.isMember(npc1));
    assertFalse(team.isMember(player3));

    assertEquals(player1, team.getMemberByName(player1.getName()).get());
    assertEquals(npc1, team.getMemberByName(npc1.getName()).get());
    assertTrue(team.getMemberByName(player3.getName()).isEmpty());

    assertEquals(Set.of(player1, player2, npc1), team.getMembers());
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

    stats = team.getMemberStats(npc1);
    assertEquals(4, stats.getRating(Skills.SPEED));
    assertEquals(3, stats.getRating(Skills.SPIRIT));
    assertEquals(2, stats.getRating(Skills.STRATEGY));
    assertEquals(13, stats.getApMaxSize());
    assertEquals(3, stats.getDefense());
    assertEquals(3, stats.getMaxWounds());
    assertEquals(5, stats.getAd());
    assertEquals(6, stats.getSd());

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
    team.getMemberStats(npc1).wound(3);
    assertTrue(team.isOut());
  }

  @Test
  public void testBuildSuccessRemovePlayer() {
    team = GrappleTeam.builder("teama")
      .member(player1)
      .member(player2)
      .removeMember(player1)
      .build();

    assertTrue(team.isMember(player2));
    assertFalse(team.isMember(player1));
  }

  @Test
  public void testBuildFailureRepeatedPlayer() {
    GrappleTeam.Builder tb = GrappleTeam.builder("teama")
      .member(player1)
      .member(player2);

    assertThrows(IllegalArgumentException.class,
                 () -> tb.member(player1));
  }

  @Test
  public void testProto() {
    ProtoModel.Team pt = team.toProto();

    assertEquals(team.getName(), pt.getName());

    assertEquals(3, pt.getMembersCount());
    Map<String, ProtoModel.Stats> ptMembers = pt.getMembersMap();
    assertTrue(ptMembers.containsKey(player1.getName()));
    assertEquals(1, ptMembers.get(player1.getName()).getBaseStats().getAd());
    assertTrue(ptMembers.containsKey(player2.getName()));
    assertEquals(3, ptMembers.get(player2.getName()).getBaseStats().getAd());
    assertTrue(ptMembers.containsKey(npc1.getName()));
    assertEquals(5, ptMembers.get(npc1.getName()).getBaseStats().getAd());

    assertEquals(1, pt.getNpcsCount());
    ProtoModel.Npc np = pt.getNpcs(0);
    assertEquals(npc1.getName(), np.getName());
    assertEquals(NPC_STATS_SPEC, np.getStatsSpec());

    GrappleTeam team2 = GrappleTeam.fromProto(pt);
    assertEquals(team, team2);

    assertEquals(team.getName(), team2.getName());

    assertEquals(3, team2.getMembers().size());
    assertTrue(team2.isMember(player1));
    assertTrue(team2.isMember(player2));
    Optional<Player> npc1_2 = team2.getMemberByName(npc1.getName());
    assertTrue(npc1_2.isPresent());

    assertEquals(1, team2.getMemberStats(player1).getAd());
    assertEquals(3, team2.getMemberStats(player2).getAd());
    assertEquals(5, team2.getMemberStats(npc1_2.get()).getAd());
  }

  // TODO: test fromProto with NPC substitution
}
