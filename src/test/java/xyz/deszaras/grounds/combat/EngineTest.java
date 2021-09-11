package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.combat.Engine.Team;
import xyz.deszaras.grounds.combat.Rules.CatchBreathInput;
import xyz.deszaras.grounds.combat.Rules.CatchBreathOutput;
import xyz.deszaras.grounds.combat.Rules.ManeuverInput;
import xyz.deszaras.grounds.combat.Rules.ManeuverOutput;
import xyz.deszaras.grounds.combat.Rules.SkillActionInput;
import xyz.deszaras.grounds.combat.Rules.SkillActionOutput;
import xyz.deszaras.grounds.combat.Rules.StrikeInput;
import xyz.deszaras.grounds.combat.Rules.StrikeOutput;
import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class EngineTest {

  private Player player1;
  private Player player2;
  private Player player3;
  private Player monster1;
  private Player monster2;
  private Team team1;
  private Team team2;
  private Rules rules;
  private Engine engine;
  private String moveResult;

  @BeforeEach
  public void setUp() {
    player1 = makeTestPlayer("player1",
                             Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE,
                             10, 3, 3, 6, 3);
    player2 = makeTestPlayer("player2",
                             Skills.INTIMIDATION, Skills.LEADERSHIP, Skills.SPEED,
                             10, 3, 3, 6, 0);
    player3 = makeTestPlayer("player3",
                             Skills.TACTICS, Skills.TAUNTING, Skills.TRICKSTER,
                             10, 3, 3, 6, 0);
    team1 = Team.builder("team1")
        .member(player1)
        .member(player2)
        .member(player3)
        .build();
    Stats p3Stats = team1.getMemberStats(player3);
    p3Stats.wound(3);

    monster1 = makeTestPlayer("monster1",
                              Skills.ENDURANCE, Skills.INTIMIDATION, Skills.SPEED,
                              6, 2, 2, 3, 0);
    monster2 = makeTestPlayer("monster2",
                              Skills.ENDURANCE, Skills.INTIMIDATION, Skills.SPEED,
                              6, 2, 2, 3, 0);
    team2 = Team.builder("team2")
        .member(monster1)
        .member(monster2)
        .build();

    rules = mock(Rules.class);
    engine = Engine.builder()
        .addTeam(team1)
        .addTeam(team2)
        .rules(rules)
        .build();
    engine.start();
  }

  @Test
  public void testStart() {
    assertEquals(1, engine.getRound());
    assertEquals("team1", engine.getMovingTeamName());
  }

  @Test
  public void testManeuver() {
    when(rules.maneuver(any(ManeuverInput.class)))
        .thenReturn(new ManeuverOutput(true, 4, 5, 2, 4, 4, 0, 7));

    moveResult = engine.move(player1, List.of("maneuver", "2", "courage"));

    ArgumentCaptor<ManeuverInput> inputCaptor =
        ArgumentCaptor.forClass(ManeuverInput.class);
    verify(rules).maneuver(inputCaptor.capture());
    ManeuverInput input = inputCaptor.getValue();
    assertEquals(team1.getMemberStats(player1), input.stats);
    assertEquals(Skills.COURAGE, input.skill);
    assertEquals(2, input.ad);
  }

  @Test
  public void testStrike() {
    when(rules.strike(any(StrikeInput.class)))
        .thenReturn(new StrikeOutput(true, 2, 3, 1, 3, 0, 1));

    moveResult = engine.move(player1, List.of("strike", "3", "monster1"));

    ArgumentCaptor<StrikeInput> inputCaptor =
        ArgumentCaptor.forClass(StrikeInput.class);
    verify(rules).strike(inputCaptor.capture());
    StrikeInput input = inputCaptor.getValue();
    assertEquals(team1.getMemberStats(player1), input.stats);
    assertEquals(3, input.sd);
    assertEquals(team2.getMemberStats(monster1), input.defenderStats);
  }

  @Test
  public void testStrikeDefenderMissing() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of("strike", "3", "monsta4")));
  }

  @Test
  public void testSkillActionSelf() {
    Stats p1Stats = team1.getMemberStats(player1);
    Stats newP1Stats = new StatsDecorator(p1Stats) {
      @Override
      public int getDefense() {
        return delegate.getDefense() + 1;
      }
    };
    when(rules.skill(any(SkillActionInput.class)))
        .thenReturn(new SkillActionOutput(true, 2, 3, 2, 3, 0, newP1Stats, null));

    moveResult = engine.move(player1, List.of("skill", "3", "endurance"));

    ArgumentCaptor<SkillActionInput> inputCaptor =
        ArgumentCaptor.forClass(SkillActionInput.class);
    verify(rules).skill(inputCaptor.capture());
    SkillActionInput input = inputCaptor.getValue();
    assertEquals(p1Stats, input.stats);
    assertEquals(3, input.sd);
    assertEquals(Skills.ENDURANCE, input.skill);
    assertNull(input.dStats);

    assertEquals(4, team1.getMemberStats(player1).getDefense());
  }

  @Test
  public void testSkillActionOther() {
    Stats m1Stats = team2.getMemberStats(monster1);
    Stats newM1Stats = new StatsDecorator(m1Stats) {
      @Override
      public int getDefense() {
        return delegate.getDefense() - 1;
      }
    };
    when(rules.skill(any(SkillActionInput.class)))
        .thenReturn(new SkillActionOutput(true, 2, 3, 2, 3, 0, null, newM1Stats));

    moveResult = engine.move(player1, List.of("skill", "3", "accuracy", "monster1"));

    ArgumentCaptor<SkillActionInput> inputCaptor =
        ArgumentCaptor.forClass(SkillActionInput.class);
    verify(rules).skill(inputCaptor.capture());
    SkillActionInput input = inputCaptor.getValue();
    assertEquals(team1.getMemberStats(player1), input.stats);
    assertEquals(3, input.sd);
    assertEquals(Skills.ACCURACY, input.skill);
    assertEquals(m1Stats, input.dStats);

    assertEquals(1, team2.getMemberStats(monster1).getDefense());
  }

  @Test
  public void testSkillActionTargetMissing() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of("skill", "3", "accuracy")));
  }

  @Test
  public void testSkillActionTargetUnnecessary() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of("skill", "3", "courage", "monster1")));
  }

  @Test
  public void testCatchBreath() {
    when(rules.catchBreath(any(CatchBreathInput.class)))
        .thenReturn(new CatchBreathOutput(2, 8));

    moveResult = engine.move(player1, List.of("catch", "breath"));

    ArgumentCaptor<CatchBreathInput> inputCaptor =
        ArgumentCaptor.forClass(CatchBreathInput.class);
    verify(rules).catchBreath(inputCaptor.capture());
    CatchBreathInput input = inputCaptor.getValue();
    assertEquals(team1.getMemberStats(player1), input.stats);
  }

  @Test
  public void testNotOnMovingTeam() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(monster1, List.of("strike", "1", "player1")));
  }

  @Test
  public void testAlreadyMoved() {
    when(rules.maneuver(any(ManeuverInput.class)))
        .thenReturn(new ManeuverOutput(true, 4, 5, 2, 4, 4, 0, 7));
    engine.move(player1, List.of("maneuver", "2", "courage"));

    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of("maneuver", "1", "courage")));
  }

  @Test
  public void testKnockedOut() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player3, List.of("maneuver", "1", "taunting")));
  }

  @Test
  public void testNoCommand() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of()));
  }

  @Test
  public void testBadCommand() {
    assertThrows(IllegalArgumentException.class,
                 () -> engine.move(player1, List.of("dance", "2")));
  }

  @Test
  public void testNextTeamAndRound() {
    when(rules.maneuver(any(ManeuverInput.class)))
        .thenReturn(new ManeuverOutput(true, 4, 5, 2, 4, 4, 0, 7));

    moveResult = engine.move(player1, List.of("maneuver", "2", "courage"));
    assertFalse(moveResult.contains("New team moving"));
    assertFalse(moveResult.contains("New round"));

    moveResult = engine.move(player2, List.of("maneuver", "2", "speed"));
    // player3 is knocked out
    assertTrue(moveResult.contains("New team moving: team2"));
    assertFalse(moveResult.contains("New round"));

    moveResult = engine.move(monster1, List.of("maneuver", "2", "speed"));
    assertFalse(moveResult.contains("New team moving"));
    assertFalse(moveResult.contains("New round"));

    moveResult = engine.move(monster2, List.of("maneuver", "2", "speed"));
    assertTrue(moveResult.contains("New team moving: team1"));
    assertTrue(moveResult.contains("New round:       2"));
  }

  private static Player makeTestPlayer(String name,
                                       Skill sk4, Skill sk3, Skill sk2,
                                       int apMaxSize, int def, int maxWounds,
                                       int ad, int sd) {
    Player p = new Player(name);
    p.setAttr(Engine.ATTR_NAME_SKILL_4, sk4.getName());
    p.setAttr(Engine.ATTR_NAME_SKILL_3, sk3.getName());
    p.setAttr(Engine.ATTR_NAME_SKILL_2, sk2.getName());
    p.setAttr(Engine.ATTR_NAME_AP_MAX_SIZE, apMaxSize);
    p.setAttr(Engine.ATTR_NAME_DEFENSE, def);
    p.setAttr(Engine.ATTR_NAME_MAX_WOUNDS, maxWounds);
    p.setAttr(Engine.ATTR_NAME_AD, ad);
    p.setAttr(Engine.ATTR_NAME_SD, sd);
    return p;
  }
}
