package xyz.deszaras.grounds.command.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class InitCombatCommandTest extends AbstractCommandTest {

  static final String COMBAT_NAME = "mortal";

  private Place location;
  private InitCombatCommand command;

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

    command = new InitCombatCommand(actor, player, COMBAT_NAME);

    String result = command.execute();
    assertNotNull(result);

    Combat combat = null;
    for (UUID id : location.getContents()) {
      Thing t = testUniverse.getThing(id).get();
      if (t instanceof Combat) {
        combat = (Combat) t;
        break;
      }
    }
    if (combat == null) {
      fail("Did not find combat in location after init");
    }

    assertEquals(player, combat.getOwner().get());
    assertEquals(COMBAT_NAME, combat.getName());
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);

    command = new InitCombatCommand(actor, player, COMBAT_NAME);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new InitCombatCommand(actor, player, COMBAT_NAME);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("initialize combat"));
  }

  @Test
  public void testFailureThereCanBeOnlyOne() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new InitCombatCommand(actor, player, COMBAT_NAME);

    String result = command.execute();
    assertNotNull(result);

    command = new InitCombatCommand(actor, player, COMBAT_NAME + "2");

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("is already present"));
  }
}
