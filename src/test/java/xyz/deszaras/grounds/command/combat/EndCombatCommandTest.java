package xyz.deszaras.grounds.command.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static xyz.deszaras.grounds.command.combat.CombatCommandTestUtils.initTestCombat;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class EndCombatCommandTest extends AbstractCommandTest {

  private Place location;
  private Combat combat;
  private EndCombatCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    player.setCurrentActor(actor);

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
    when(combat.getAllCombatants()).thenReturn(Set.of(player));

    command = new EndCombatCommand(actor, player);

    String result = command.execute();
    assertNotNull(result);
    verify(combat).end();

    for (UUID id : location.getContents()) {
      Thing t = testUniverse.getThing(id).get();
      if (t instanceof Combat) {
        fail("Found combat in location after end");
      }
    }
    assertTrue(testUniverse.getThing(combat.getId()).isEmpty());

    Message message = player.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals("Combat has ended.", message.getMessage());
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);

    command = new EndCombatCommand(actor, player);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoCombat() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new EndCombatCommand(actor, player);

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

    command = new EndCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("You lack"));
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new EndCombatCommand(actor, player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("end combat"));
  }
}
