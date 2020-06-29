package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

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

    location = mock(Place.class);

    thing = mock(Thing.class);
    command = new DropCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.has(thing)).thenReturn(true);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(player.getLocation()).thenReturn(Optional.of(location));

    assertTrue(command.execute());

    verify(player).take(thing);
    verify(location).give(thing);
    verify(thing).setLocation(location);
  }

  @Test
  public void testFailureDoNotHave() throws Exception {
    when(player.has(thing)).thenReturn(false);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You aren't holding that", e.getMessage());
  }

  @Test
  public void testFailureUndroppable() throws Exception {
    when(player.has(thing)).thenReturn(true);
    when(thing.passes(Category.GENERAL, player)).thenReturn(false);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are unable to drop that", e.getMessage());
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    when(player.has(thing)).thenReturn(true);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(player.getLocation()).thenReturn(Optional.empty());

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You have no location, so you may not drop anything",
                 e.getMessage());
  }
}
