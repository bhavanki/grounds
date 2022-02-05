package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

@SuppressWarnings("PMD.TooManyStaticImports")
public class PlayerSelectionTest {

  private static final String USERNAME = "PlayerSelectionTest.actor1";
  private static final String USERNAME2 = "PlayerSelectionTest.actor2";

  private LineReader lineReader;
  private PrintWriter out;
  private Actor actor;
  private Universe universe;

  private PlayerSelection playerSelection;

  private Player player1;
  private Player player2;
  private Player player3;

  @BeforeEach
  public void setUp() {
    ActorDatabase.INSTANCE.createActorRecord(USERNAME, "password");
    ActorDatabase.INSTANCE.createActorRecord(USERNAME2, "password2");

    lineReader = mock(LineReader.class);
    out = mock(PrintWriter.class);
    actor = new Actor(USERNAME);
    universe = new Universe("PlayerSelectionTest.universe");

    playerSelection = new PlayerSelection(lineReader, out, actor, universe);

    player1 = new Player("player1");
    universe.addThing(player1);
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.addPlayer(player1.getId()));
    player2 = new Player("player2");
    universe.addThing(player2);
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.addPlayer(player2.getId()));
    player3 = new Player("zplayer3");
    universe.addThing(player3);
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.addPlayer(player3.getId()));
  }

  @AfterEach
  public void tearDown() {
    ActorDatabase.INSTANCE.removeActorRecord(USERNAME);
    ActorDatabase.INSTANCE.removeActorRecord(USERNAME2);
  }

  @Test
  public void testSelectionByName() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("player1");

    assertEquals(player1, playerSelection.call());

    verifyOutput("player1");
  }

  @Test
  public void testSelectionByNumber() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("2");

    assertEquals(player2, playerSelection.call());

    verifyOutput("player2");
  }

  @Test
  public void testSelectionByPrefix() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("z");

    assertEquals(player3, playerSelection.call());

    verifyOutput("zplayer3");
  }

  @Test
  public void testSelectionByNameBlankLine() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("").thenReturn("player1");

    assertEquals(player1, playerSelection.call());

    verifyOutput("player1");
  }

  private void verifyOutput(String selectedPlayerName) {
    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verify(out).printf(PlayerSelection.SELECTING_FORMAT, selectedPlayerName);
  }

  private void verifyPermittedPlayers(InOrder outputOrder) {
    outputOrder.verify(out).println(PlayerSelection.PERMITTED_PLAYERS);
    outputOrder.verify(out).printf("  %d. %s\n", 1, "player1");
    outputOrder.verify(out).printf("  %d. %s\n", 2, "player2");
    outputOrder.verify(out).printf("  %d. %s\n", 3, "zplayer3");
    outputOrder.verify(out).println("");
  }

  @Test
  public void testAutoSelection() {
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.removePlayer(player2.getId()));
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.removePlayer(player3.getId()));

    assertEquals(player1, playerSelection.call());

    InOrder outputOrder = inOrder(out);
    outputOrder.verify(out).println(PlayerSelection.PERMITTED_PLAYERS);
    outputOrder.verify(out).printf("  %d. %s\n", 1, "player1");
    outputOrder.verify(out).printf(PlayerSelection.AUTO_SELECTING_FORMAT, "player1");
    outputOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testExit() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("exit");

    assertNull(playerSelection.call());

    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testExitWithNull() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn(null);

    assertNull(playerSelection.call());

    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testInvalidName() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("number2").thenReturn("player2");

    assertEquals(player2, playerSelection.call());

    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verify(out).print(contains(String.format(PlayerSelection.INVALID_PLAYER_NAME_FORMAT, "number2")));
    outputOrder.verify(out).printf(PlayerSelection.SELECTING_FORMAT, "player2");
  }

  @Test
  public void testInvalidNumber() {
    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("4").thenReturn("3");

    assertEquals(player3, playerSelection.call());

    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verify(out).print(contains(String.format(PlayerSelection.INVALID_PLAYER_NUMBER_FORMAT, 4)));
    outputOrder.verify(out).printf(PlayerSelection.SELECTING_FORMAT, "zplayer3");
  }

  @Test
  public void testOccupied() {
    Actor actor2 = new Actor(USERNAME2);
    player2.setCurrentActor(actor2);

    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("player2").thenReturn("player1");

    assertEquals(player1, playerSelection.call());

    InOrder outputOrder = inOrder(out);
    verifyPermittedPlayers(outputOrder);
    outputOrder.verify(out).print(contains(String.format(PlayerSelection.OCCUPIED_FORMAT, "player2")));
    outputOrder.verify(out).printf(PlayerSelection.SELECTING_FORMAT, "player1");
  }

  @Test
  public void testAutoSelectionOccupied() {
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.removePlayer(player2.getId()));
    ActorDatabase.INSTANCE.updateActorRecord(USERNAME, r -> r.removePlayer(player3.getId()));
    Actor actor2 = new Actor(USERNAME2);
    player1.setCurrentActor(actor2);

    when(lineReader.readLine(PlayerSelection.SELECTION_PROMPT)).thenReturn("exit");

    assertNull(playerSelection.call());

    InOrder outputOrder = inOrder(out);
    outputOrder.verify(out).println(PlayerSelection.PERMITTED_PLAYERS);
    outputOrder.verify(out).printf("  %d. %s\n", 1, "player1");
    outputOrder.verify(out).printf(PlayerSelection.AUTO_SELECTING_FORMAT, "player1");
    outputOrder.verify(out).print(contains(String.format(PlayerSelection.OCCUPIED_FORMAT, "player1")));
    outputOrder.verifyNoMoreInteractions();
  }
}
