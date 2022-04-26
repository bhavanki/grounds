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

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.combat.Engine;
import xyz.deszaras.grounds.combat.System;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class RestoreCombatCommandTest extends AbstractCommandTest {

  private static final byte[] STATE = "state".getBytes();
  private static final String STATE_STRING = Base64.getEncoder().encodeToString(STATE);

  private Place location;
  private Engine engine;
  private System system;
  private Combat combat;
  private RestoreCombatCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    player.setCurrentActor(actor);

    system = mock(System.class);
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
    when(combat.getState()).thenReturn(Optional.of(STATE_STRING));
    when(combat.getSystem()).thenReturn(system);
    engine = mock(Engine.class);
    when(system.restore(STATE)).thenReturn(engine);

    command = new RestoreCombatCommand(actor, player);

    String result = command.execute();
    assertEquals("Combat restored", result);

    verify(combat).setEngine(engine);
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);

    command = new RestoreCombatCommand(actor, player);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoCombat() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new RestoreCombatCommand(actor, player);

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

    command = new RestoreCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("You lack"));
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new RestoreCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("restore combat"));
  }

  @Test
  public void testFailureNoState() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getState()).thenReturn(Optional.empty());

    command = new RestoreCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Combat state not found", e.getMessage());
  }


  @Test
  public void testFailureInvalidState() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getState()).thenReturn(Optional.of(STATE_STRING));
    when(combat.getSystem()).thenReturn(system);
    when(system.restore(STATE))
        .thenThrow(new IllegalArgumentException());

    command = new RestoreCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Failed to restore combat state", e.getMessage());
  }

  @Test
  public void testFailureNotSupported() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.getState()).thenReturn(Optional.of(STATE_STRING));
    when(combat.getSystem()).thenReturn(system);
    when(system.restore(STATE))
        .thenThrow(new UnsupportedOperationException());

    command = new RestoreCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Failed to restore combat state", e.getMessage());
  }

}
