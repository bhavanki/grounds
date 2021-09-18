package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CombatTest {

  private Player bill;
  private Player ted;
  private Player bud;
  private Player lou;
  private Player thelma;
  private Player louise;
  private Combat combat;
  private Engine mockEngine;

  @BeforeEach
  public void setUp() throws Exception {
    bill = EngineTest.makeTestPlayer("bill",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);
    ted = EngineTest.makeTestPlayer("ted",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);

    bud = EngineTest.makeTestPlayer("bud",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);
    lou = EngineTest.makeTestPlayer("lou",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);

    thelma = EngineTest.makeTestPlayer("thelma",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);
    louise = EngineTest.makeTestPlayer("louise",
        Skills.ACCURACY, Skills.COURAGE, Skills.ENDURANCE, 10, 3, 3, 9, 5);

    combat = new Combat("fight");
    mockEngine = mock(Engine.class);
  }

  @Test
  public void testStart() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .addPlayer(thelma, "team3")
        .addPlayer(ted, "team1")
        .addPlayer(louise, "team3")
        .start(List.of("team1", "team2"));

    Engine e = combat.getEngine();
    assertNotNull(e);

    List<Team> teams = e.getTeams();
    assertEquals(3, teams.size());

    Team team1 = teams.get(0);
    assertEquals("team1", team1.getName());
    assertEquals(Set.of(bill, ted), team1.getMembers());

    Team team2 = teams.get(1);
    assertEquals("team2", team2.getName());
    assertEquals(Set.of(bud, lou), team2.getMembers());

    Team team3 = teams.get(2);
    assertEquals("team3", team3.getName());
    assertEquals(Set.of(thelma, louise), team3.getMembers());
  }

  @Test
  public void testRemovePlayer() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .removePlayer(lou, "team2")
        .addPlayer(ted, "team1")
        .removePlayer("bill", "team1")
        .start(List.of("team1", "team2"));

    List<Team> teams = combat.getEngine().getTeams();
    assertEquals(2, teams.size());

    Team team1 = teams.get(0);
    assertEquals(Set.of(ted), team1.getMembers());

    Team team2 = teams.get(1);
    assertEquals(Set.of(bud), team2.getMembers());
  }

  @Test
  public void testFailIfStarted() {
    combat.addPlayer(bill, "team1")
        .addPlayer(bud, "team2")
        .start(List.of());

    assertThrows(IllegalStateException.class,
                 () -> combat.addPlayer(ted, "team1"));
    assertThrows(IllegalStateException.class,
                 () -> combat.removePlayer(bill, "team1"));
    assertThrows(IllegalStateException.class,
                 () -> combat.removePlayer("bud", "team2"));
    assertThrows(IllegalStateException.class,
                 () -> combat.start(List.of()));
  }

  @Test
  public void testGetAllCombatantsPreStart() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .addPlayer(thelma, "team3")
        .addPlayer(ted, "team1")
        .addPlayer(louise, "team3");

    Set<Player> combatants = combat.getAllCombatants();
    assertEquals(Set.of(bill, ted, bud, lou, thelma, louise), combatants);
  }

  @Test
  public void testGetAllCombatantsPostStart() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .addPlayer(thelma, "team3")
        .addPlayer(ted, "team1")
        .addPlayer(louise, "team3")
        .start(List.of());

    Set<Player> combatants = combat.getAllCombatants();
    assertEquals(Set.of(bill, ted, bud, lou, thelma, louise), combatants);
  }

  @Test
  public void testStatusPreStartNoPlayers() {
    assertEquals(Combat.STATUS_NO_PLAYERS, combat.status());
  }

  @Test
  public void testStatusPreStartPlayers() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2");

    String status = combat.status();

    assertTrue(status.contains(Combat.STATUS_PRE_START));
    assertTrue(status.contains("bill"));
    assertTrue(status.contains("team1"));
    assertTrue(status.contains("lou"));
    assertTrue(status.contains("team2"));
  }

  @Test
  public void testStatusPostStart() {
    when(mockEngine.status()).thenReturn("status");
    combat.setEngine(mockEngine);

    assertEquals("status", combat.status());
  }

  @Test
  public void testEndNotStarted() {
    combat.end();
    // nothing to verify
  }

  @Test
  public void testEndStarted() {
    combat.setEngine(mockEngine);

    combat.end();

    verify(mockEngine).end();
  }

  private static final List<String> MOVE_COMMAND = List.of("catch", "breath");

  @Test
  public void testFailIfNotStarted() {
    assertThrows(IllegalStateException.class,
                 () -> combat.move("ted", MOVE_COMMAND));
    assertThrows(IllegalStateException.class,
                 () -> combat.move(ted, MOVE_COMMAND));
    assertThrows(IllegalStateException.class,
                 () -> combat.resolveRound());
  }

  @Test
  public void testMoveByPlayer() {
    when(mockEngine.move(ted, MOVE_COMMAND)).thenReturn("OK");
    combat.setEngine(mockEngine);

    assertEquals("OK", combat.move(ted, MOVE_COMMAND));
  }

  @Test
  public void testMoveByPlayerName() {
    when(mockEngine.move("ted", MOVE_COMMAND)).thenReturn("OK");
    combat.setEngine(mockEngine);

    assertEquals("OK", combat.move("ted", MOVE_COMMAND));
  }

  @Test
  public void testResolveRound() {
    when(mockEngine.resolveRound()).thenReturn("OK");
    combat.setEngine(mockEngine);

    assertEquals("OK", combat.resolveRound());
  }
}
