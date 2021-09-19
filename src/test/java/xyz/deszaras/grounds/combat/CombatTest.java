package xyz.deszaras.grounds.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CombatTest {

  private Player bill;
  private Player ted;
  private Player bud;
  private Player lou;
  private Player thelma;
  private Player louise;
  private System mockSystem;
  private Combat combat;
  private Team team1;
  private Team team2;
  private Team team3;
  private Team.Builder mockTeamBuilder1;
  private Team.Builder mockTeamBuilder2;
  private Team.Builder mockTeamBuilder3;
  private Engine mockEngine;
  private Engine.Builder mockEngineBuilder;

  @BeforeEach
  public void setUp() throws Exception {
    bill = makeTestPlayer("bill");
    ted = makeTestPlayer("ted");
    bud = makeTestPlayer("bud");
    lou = makeTestPlayer("lou");
    thelma = makeTestPlayer("thelma");
    louise = makeTestPlayer("louise");

    mockSystem = mock(System.class);
    combat = new Combat("fight", mockSystem);

    team1 = mock(Team.class);
    team2 = mock(Team.class);
    team3 = mock(Team.class);
    mockTeamBuilder1 = mock(Team.Builder.class);
    when(mockTeamBuilder1.member(any(Player.class))).thenReturn(mockTeamBuilder1);
    when(mockTeamBuilder1.removeMember(any(Player.class))).thenReturn(mockTeamBuilder1);
    when(mockTeamBuilder1.removeMember(any(String.class))).thenReturn(mockTeamBuilder1);
    when(mockTeamBuilder1.build()).thenReturn(team1);
    mockTeamBuilder2 = mock(Team.Builder.class);
    when(mockTeamBuilder2.member(any(Player.class))).thenReturn(mockTeamBuilder2);
    when(mockTeamBuilder2.removeMember(any(Player.class))).thenReturn(mockTeamBuilder2);
    when(mockTeamBuilder2.removeMember(any(String.class))).thenReturn(mockTeamBuilder2);
    when(mockTeamBuilder2.build()).thenReturn(team2);
    mockTeamBuilder3 = mock(Team.Builder.class);
    when(mockTeamBuilder3.member(any(Player.class))).thenReturn(mockTeamBuilder3);
    when(mockTeamBuilder3.removeMember(any(Player.class))).thenReturn(mockTeamBuilder3);
    when(mockTeamBuilder3.removeMember(any(String.class))).thenReturn(mockTeamBuilder3);
    when(mockTeamBuilder3.build()).thenReturn(team3);

    when(mockSystem.getTeamBuilder("team1")).thenReturn(mockTeamBuilder1);
    when(mockSystem.getTeamBuilder("team2")).thenReturn(mockTeamBuilder2);
    when(mockSystem.getTeamBuilder("team3")).thenReturn(mockTeamBuilder3);

    mockEngine = mock(Engine.class);
    mockEngineBuilder = mock(Engine.Builder.class);
    when(mockEngineBuilder.build()).thenReturn(mockEngine);

    when(mockSystem.getEngineBuilder()).thenReturn(mockEngineBuilder);
  }

  @Test
  public void testAddPlayer() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .addPlayer(thelma, "team3")
        .addPlayer(ted, "team1")
        .addPlayer(louise, "team3");

    verify(mockTeamBuilder1).member(bill);
    verify(mockTeamBuilder1).member(ted);
    verify(mockTeamBuilder2).member(lou);
    verify(mockTeamBuilder2).member(bud);
    verify(mockTeamBuilder3).member(thelma);
    verify(mockTeamBuilder3).member(louise);
  }

  @Test
  public void testRemovePlayer() {
    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2")
        .addPlayer(bud, "team2")
        .removePlayer(lou, "team2")
        .addPlayer(ted, "team1")
        .removePlayer("bill", "team1");

    verify(mockTeamBuilder2).removeMember(lou);
    verify(mockTeamBuilder1).removeMember("bill");
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

    assertEquals(mockEngine, combat.getEngine());

    InOrder teamsInOrder = inOrder(mockEngineBuilder);
    teamsInOrder.verify(mockEngineBuilder).addTeam(team1);
    teamsInOrder.verify(mockEngineBuilder).addTeam(team2);
    teamsInOrder.verify(mockEngineBuilder).addTeam(team3);

    verify(mockEngine).start();
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
    when(mockTeamBuilder1.getMembers()).thenReturn(Set.of(bill, ted));
    when(mockTeamBuilder2.getMembers()).thenReturn(Set.of(lou, bud));
    when(mockTeamBuilder3.getMembers()).thenReturn(Set.of(thelma, louise));

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
    when(mockEngine.getTeams()).thenReturn(List.of(team1, team2, team3));
    combat.setEngine(mockEngine);

    when(team1.getMembers()).thenReturn(Set.of(bill, ted));
    when(team2.getMembers()).thenReturn(Set.of(lou, bud));
    when(team3.getMembers()).thenReturn(Set.of(thelma, louise));

    Set<Player> combatants = combat.getAllCombatants();
    assertEquals(Set.of(bill, ted, bud, lou, thelma, louise), combatants);
  }

  @Test
  public void testStatusPreStartNoPlayers() {
    assertEquals(Combat.STATUS_NO_PLAYERS, combat.status());
  }

  @Test
  public void testStatusPreStartPlayers() {
    when(mockTeamBuilder1.status()).thenReturn("Team 1 present");
    when(mockTeamBuilder2.status()).thenReturn("Team 2 present");

    combat.addPlayer(bill, "team1")
        .addPlayer(lou, "team2");

    String status = combat.status();

    assertTrue(status.contains(Combat.STATUS_PRE_START));
    assertTrue(status.contains("Team 1 present"));
    assertTrue(status.contains("Team 2 present"));
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

  private Player makeTestPlayer(String name) {
    Player p = mock(Player.class);
    when(p.getName()).thenReturn(name);
    return p;
  }
}
