package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.TeleportCommand.TeleportArrivalEvent;
import xyz.deszaras.grounds.command.TeleportCommand.TeleportDepartureEvent;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class MoveCommandTest extends AbstractCommandTest {

  private Place locationFrom;
  private Place locationTo;
  private Link link;
  private MoveCommand command;
  private TeleportCommand testTeleportCommand;
  private TeleportDepartureEvent departureEvent;
  private TeleportArrivalEvent arrivalEvent;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    locationFrom = newTestPlace("hither");
    locationTo = newTestPlace("yon");
    link = newTestLink("linky", locationFrom, "H", locationTo, "Y");

    command = new MoveCommand(actor, player, "Y");

    testTeleportCommand = mock(TeleportCommand.class);
    try {
      when(testTeleportCommand.executeImpl()).thenReturn("here");
      command.setTestTeleportCommand(testTeleportCommand);
    } catch (CommandException e) {
      fail(e.getMessage());
    }
    // does not verify that the real teleport command is for the destination :(

    departureEvent = new TeleportDepartureEvent(player, locationFrom);
    arrivalEvent = new TeleportArrivalEvent(player, locationTo);
    when(testTeleportCommand.getEvents())
        .thenReturn(Set.of(departureEvent, arrivalEvent));
  }

  @Test
  public void testSuccess() throws Exception {
    locationFrom.give(player);
    player.setLocation(locationFrom);

    assertEquals("here", command.execute());

    verifyEvent(departureEvent, command);
    verifyEvent(arrivalEvent, command);
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("move somewhere else"));
  }

  @Test
  public void testFailureNoExitWithName() throws Exception {
    locationFrom.give(player);
    player.setLocation(locationFrom);

    command = new MoveCommand(actor, player, "X");

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("I can't see an exit"));
  }

  @Test
  public void testFailureMalformedLink() throws Exception {
    locationFrom.give(player);
    player.setLocation(locationFrom);

    Place locationElsewhere = new Place("elsewhere"); // do not add to universe
    newTestLink("linky2", locationFrom, "H", locationElsewhere, "E");

    command = new MoveCommand(actor, player, "E");

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("I can't find that place"));
  }

  @Test
  public void testFailurePermission() throws Exception {
    locationFrom.give(player);
    player.setLocation(locationFrom);

    Policy linkPolicy = link.getPolicy();
    linkPolicy.setRoles(Policy.Category.USE, Role.WIZARD_ROLES);

    assertThrows(PermissionException.class, () -> command.execute());
  }
}
