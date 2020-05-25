package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

public class DropCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Place location;
  private Thing thing;
  private DropCommand command;

  @Before
  public void setUp() {
    super.setUp();

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

    thrown.expect(CommandException.class);
    thrown.expectMessage("You aren't holding that");

    command.execute();
  }

  @Test
  public void testFailureUndroppable() throws Exception {
    when(player.has(thing)).thenReturn(true);
    when(thing.passes(Category.GENERAL, player)).thenReturn(false);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are unable to drop that");

    command.execute();
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    when(player.has(thing)).thenReturn(true);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(player.getLocation()).thenReturn(Optional.empty());

    thrown.expect(CommandException.class);
    thrown.expectMessage("You are not located anywhere, so you may not drop anything");

    command.execute();
  }
}
