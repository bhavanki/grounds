package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class HomeCommandTest extends AbstractCommandTest {

  private Place home;
  private HomeCommand command;
  private TeleportCommand testTeleportCommand;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    home = newTestPlace("home");

    testTeleportCommand = mock(TeleportCommand.class);
    try {
      when(testTeleportCommand.executeImpl()).thenReturn("BAMF");
    } catch (CommandException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTeleportHome() throws Exception {
    player.setHome(home);
    command = new HomeCommand(actor, player, null);
    command.setTestTeleportCommand(testTeleportCommand);

    assertEquals("BAMF", command.execute());
  }

  @Test
  public void testTeleportNoHome() throws Exception {
    command = new HomeCommand(actor, player, null);
    command.setTestTeleportCommand(testTeleportCommand);

    assertThrows(CommandException.class, () -> command.executeImpl());
  }

  @Test
  public void testSetHome() throws Exception {
    assertTrue(player.getHome().isEmpty());
    command = new HomeCommand(actor, player, home);
    command.setTestTeleportCommand(testTeleportCommand);

    assertEquals("Home set", command.executeImpl());

    assertEquals(home, player.getHome().get());
  }
}
