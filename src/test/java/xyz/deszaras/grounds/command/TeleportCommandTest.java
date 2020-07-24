package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.TeleportCommand.TeleportArrivalEvent;
import xyz.deszaras.grounds.command.TeleportCommand.TeleportDepartureEvent;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class TeleportCommandTest extends AbstractCommandTest {

  private Place source;
  private Place destination;
  private TeleportCommand command;
  private LookCommand testLookCommand;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    source = newTestPlace("src");
    destination = newTestPlace("dest");

    command = new TeleportCommand(actor, player, destination);

    testLookCommand = mock(LookCommand.class);
    try {
      when(testLookCommand.executeImpl()).thenReturn("here");
      command.setTestLookCommand(testLookCommand);
    } catch (CommandException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSuccess() throws Exception {
    player.setLocation(source);
    source.give(player);

    assertEquals("here", command.execute());

    assertFalse(source.has(player));
    assertTrue(destination.has(player));
    assertEquals(destination, player.getLocation().get());

    verifyEvent(new TeleportDepartureEvent(player, source), command);
    verifyEvent(new TeleportArrivalEvent(player, destination), command);
  }

  @Test
  public void testSuccessNoSource() throws Exception {
    assertEquals("here", command.execute());

    assertTrue(destination.has(player));
    assertEquals(destination, player.getLocation().get());

    verifyEvent(new TeleportArrivalEvent(player, destination), command);
  }

  @Test
  public void testFailureNotPermitted() throws Exception {
    player.setLocation(source);
    source.give(player);
    destination.getPolicy().setRoles(Category.GENERAL, Role.WIZARD_ROLES);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to move there", e.getMessage());
  }
}
