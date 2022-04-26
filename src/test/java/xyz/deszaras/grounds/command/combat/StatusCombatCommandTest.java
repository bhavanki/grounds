package xyz.deszaras.grounds.command.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static xyz.deszaras.grounds.command.combat.CombatCommandTestUtils.initTestCombat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class StatusCombatCommandTest extends AbstractCommandTest {

  private Place location;
  private Combat combat;
  private StatusCombatCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    location = newTestPlace("arena");
  }

  @Test
  public void testSuccess() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.status()).thenReturn("nifty");

    command = new StatusCombatCommand(actor, player);

    String result = command.execute();
    assertEquals("nifty", result);
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);

    command = new StatusCombatCommand(actor, player);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoCombat() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new StatusCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("No combat is present"));
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new StatusCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("get combat status"));
  }
}
