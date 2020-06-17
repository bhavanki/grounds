package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

public class TakeCommandTest extends AbstractCommandTest {

  private Place location;
  private Thing thing;
  private TakeCommand command;

  @BeforeEach
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

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You are already holding that",
                 e.getMessage());
  }

  @Test
  public void testFailureNotAPlainThing() throws Exception {
    when(player.has(location)).thenReturn(false);

    command = new TakeCommand(actor, player, location);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You can only take ordinary things",
                 e.getMessage());
  }

  @Test
  public void testFailureUntakable() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(false);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to take that",
                 e.getMessage());
  }

  @Test
  public void testFailureNoThingLocation() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.empty());
    when(player.getLocation()).thenReturn(Optional.of(location));

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }

  @Test
  public void testFailureNoPlayerLocation() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocation()).thenReturn(Optional.empty());

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
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

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }
}
