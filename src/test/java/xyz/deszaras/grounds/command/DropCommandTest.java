package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class DropCommandTest extends AbstractCommandTest {

  private Place location;
  private Thing thing;
  private DropCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    location = newTestPlace("there");

    thing = newTestThing("saber");
    command = new DropCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    player.give(thing);
    thing.setLocation(player);
    location.give(player);
    player.setLocation(location);

    assertTrue(command.execute());

    assertFalse(player.has(thing));
    assertTrue(location.has(thing));
    assertEquals(location, thing.getLocation().get());
  }

  @Test
  public void testFailureDoNotHave() throws Exception {
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You aren't holding that", e.getMessage());
  }

  @Test
  public void testFailureUndroppable() throws Exception {
    player.give(thing);
    thing.setLocation(player);
    thing.getPolicy().setRoles(Category.GENERAL, Role.WIZARD_ROLES);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are unable to drop that", e.getMessage());
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    player.give(thing);
    thing.setLocation(player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You have no location, so you may not drop anything",
                 e.getMessage());
  }
}
