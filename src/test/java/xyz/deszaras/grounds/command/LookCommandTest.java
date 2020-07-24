package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class LookCommandTest extends AbstractCommandTest {

  private Place location;
  private LookCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    location = newTestPlace("somewhere");

    command = new LookCommand(actor, player);
  }

  @Test
  public void testSuccess() throws Exception {
    location.give(player);
    player.setLocation(location);

    String lookResult = command.execute();

    assertTrue(lookResult.contains("somewhere"));
  }

  @Test
  public void testFailureCannotLook() throws Exception {
    location.give(player);
    player.setLocation(location);
    location.getPolicy().setRoles(Category.READ, Role.WIZARD_ROLES);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to look at where you are",
                 e.getMessage());
  }

  @Test
  public void testSuccessNowhere() throws Exception {
    String lookResult = command.execute();

    assertTrue(lookResult.contains("nowhere"));
  }
}
