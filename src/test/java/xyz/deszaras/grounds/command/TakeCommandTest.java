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
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

public class TakeCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Place location;
  private Thing thing;
  private TakeCommand command;

  @Before
  public void setUp() {
    super.setUp();
    setPlayerRoles(Role.DENIZEN);

    location = mock(Place.class);

    thing = mock(Thing.class);
    command = new TakeCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocation()).thenReturn(Optional.of(location));

    assertTrue(command.execute());

    verify(location).take(thing);
    verify(player).give(thing);
    verify(thing).setLocation(null);
  }

  @Test
  public void testFailureAlreadyHave() throws Exception {
    when(player.has(thing)).thenReturn(true);

    thrown.expect(CommandException.class);
    thrown.expectMessage("You are already holding that");

    command.execute();
  }

  @Test
  public void testFailureNotAPlainThing() throws Exception {
    when(player.has(location)).thenReturn(false);

    thrown.expect(CommandException.class);
    thrown.expectMessage("You can only take ordinary things");

    command = new TakeCommand(actor, player, location);
    command.execute();
  }

  @Test
  public void testFailureUntakable() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(false);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to take that");

    command.execute();
  }

  @Test
  public void testFailureNoThingLocation() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.empty());
    when(player.getLocation()).thenReturn(Optional.of(location));

    thrown.expect(CommandException.class);
    thrown.expectMessage("You may only take that if you are in the same location");

    command.execute();
  }

  @Test
  public void testFailureNoPlayerLocation() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocation()).thenReturn(Optional.empty());

    thrown.expect(CommandException.class);
    thrown.expectMessage("You may only take that if you are in the same location");

    command.execute();
  }

  @Test
  public void testSuccessWizardNotCollocated() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocation()).thenReturn(Optional.of(mock(Place.class)));

    setPlayerRoles(Role.BARD);

    assertTrue(command.execute());

    verify(location).take(thing);
    verify(player).give(thing);
    verify(thing).setLocation(null);
  }

  @Test
  public void testFailureNotCollocated() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocation()).thenReturn(Optional.of(mock(Place.class)));

    thrown.expect(CommandException.class);
    thrown.expectMessage("You may only take that if you are in the same location");

    command.execute();
  }
}
