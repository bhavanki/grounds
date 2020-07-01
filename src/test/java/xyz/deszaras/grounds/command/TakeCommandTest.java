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
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
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
    when(player.getLocationAsPlace()).thenReturn(Optional.of(location));

    assertTrue(command.execute());

    verify(location).take(thing);
    verify(player).give(thing);
    verify(thing).setLocation(player);
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
  public void testFailureAlreadyHaveNested() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    Thing rucksack = mock(Thing.class);
    when(rucksack.getLocation()).thenReturn(Optional.of(player));
    when(thing.getLocation()).thenReturn(Optional.of(rucksack));

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
    when(player.getLocationAsPlace()).thenReturn(Optional.of(location));

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
    when(player.getLocationAsPlace()).thenReturn(Optional.empty());

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }

  @Test
  public void testSuccessSeize() throws Exception {
    Player holder = mock(Player.class);

    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(holder));
    when(player.getLocationAsPlace()).thenReturn(Optional.of(location));

    setPlayerRoles(Role.ADEPT);

    assertTrue(command.execute());

    verify(holder).take(thing);
    verify(player).give(thing);
    verify(thing).setLocation(player);
  }

  @Test
  public void testFailureSeize() throws Exception {
    Player holder = mock(Player.class);

    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(holder));
    when(player.getLocationAsPlace()).thenReturn(Optional.of(location));

    setPlayerRoles(Role.BARD);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may not take something from another player",
                 e.getMessage());
  }

  @Test
  public void testSuccessWizardNotCollocated() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocationAsPlace()).thenReturn(Optional.of(mock(Place.class)));

    setPlayerRoles(Role.BARD);

    assertTrue(command.execute());

    verify(location).take(thing);
    verify(player).give(thing);
    verify(thing).setLocation(player);
  }

  @Test
  public void testFailureNotCollocated() throws Exception {
    when(player.has(thing)).thenReturn(false);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);
    when(thing.getLocation()).thenReturn(Optional.of(location));
    when(player.getLocationAsPlace()).thenReturn(Optional.of(mock(Place.class)));

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }
}
