package xyz.deszaras.grounds.command.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static xyz.deszaras.grounds.command.combat.CombatCommandTestUtils.initTestCombat;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.combat.Engine;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class SaveCombatCommandTest extends AbstractCommandTest {

  private static final byte[] STATE = "state".getBytes();
  private static final String STATE_STRING = Base64.getEncoder().encodeToString(STATE);

  private Place location;
  private Engine engine;
  private Combat combat;
  private SaveCombatCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    engine = mock(Engine.class);
    location = newTestPlace("arena");
  }

  @Test
  public void testSuccess() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getEngine()).thenReturn(engine);
    when(engine.getState()).thenReturn(STATE);

    command = new SaveCombatCommand(actor, player);

    String result = command.execute();
    assertEquals("Combat saved", result);

    ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
    verify(combat).setState(stateCaptor.capture());
    assertEquals(STATE_STRING, stateCaptor.getValue());
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);

    command = new SaveCombatCommand(actor, player);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoCombat() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new SaveCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("No combat is present"));
  }

  @Test
  public void testFailureDueToNotOwner() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.empty());
    when(combat.passes(Category.WRITE, player)).thenReturn(false);

    command = new SaveCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("You lack"));
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new SaveCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("save combat"));
  }

  @Test
  public void testFailureNotStarted() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getEngine()).thenReturn(null);

    command = new SaveCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Combat has not yet started", e.getMessage());
  }

  @Test
  public void testFailureNotSupported() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getEngine()).thenReturn(engine);
    when(engine.getState()).thenReturn(null);

    command = new SaveCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Saving combat is not available", e.getMessage());
  }

}
