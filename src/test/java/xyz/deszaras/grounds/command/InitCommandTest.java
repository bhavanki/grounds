package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class InitCommandTest extends AbstractCommandTest {

  private static final String UNIVERSE_NAME = "Earth-616";

  private InitCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    command = new InitCommand(actor, Player.GOD, UNIVERSE_NAME);
  }

  @Test
  public void testSuccess() throws Exception {
    assertTrue(command.execute());

    Universe universe = Universe.getCurrent();
    assertEquals(UNIVERSE_NAME, universe.getName());
    assertNull(Universe.getCurrentFile());

    Place origin = universe.getOriginPlace();
    assertTrue(origin.getDescription().isPresent());
    Place laf = universe.getLostAndFoundPlace();
    assertTrue(laf.getDescription().isPresent());
    Place gh = universe.getGuestHomePlace();
    assertTrue(gh.getDescription().isPresent());

    Policy policy = gh.getPolicy();
    assertEquals(Role.ALL_ROLES, policy.getRoles(Policy.Category.GENERAL));
    assertEquals(Role.ALL_ROLES, policy.getRoles(Policy.Category.READ));

    assertEquals(Player.GOD, universe.getThing(Player.GOD.getId()).get());
    assertEquals(origin, Player.GOD.getLocation().get());
    assertTrue(origin.getContents().contains(Player.GOD.getId()));
  }

  @Test
  public void testFailureNotGOD() throws Exception {
    command = new InitCommand(actor, player, UNIVERSE_NAME);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNotVOID() throws Exception {
    command = new InitCommand(actor, Player.GOD, Universe.VOID.getName());

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may not recreate the VOID universe",
                 e.getMessage());
  }
}
