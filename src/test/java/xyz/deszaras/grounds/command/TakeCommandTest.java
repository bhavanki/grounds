package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.TakeCommand.TakenThing;
import xyz.deszaras.grounds.command.TakeCommand.TakenThingEvent;
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

    location = newTestPlace("there");

    thing = newTestThing("saber");
    command = new TakeCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    location.give(thing);
    thing.setLocation(location);
    location.give(player);
    player.setLocation(location);

    assertTrue(command.execute());

    assertFalse(location.has(thing));
    assertTrue(player.has(thing));
    assertEquals(player, thing.getLocation().get());

    TakenThingEvent takeEvent =
        verifyEvent(new TakenThingEvent(player, location, thing), command);
    assertEquals(thing.getName(), ((TakenThing) takeEvent.getPayload()).thingName);
    assertEquals(thing.getId().toString(), ((TakenThing) takeEvent.getPayload()).thingId);
  }

  @Test
  public void testFailureAlreadyHave() throws Exception {
    player.give(thing);
    thing.setLocation(player);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You are already holding that",
                 e.getMessage());
  }

  @Test
  public void testFailureAlreadyHaveNested() throws Exception {
    Thing rucksack = newTestThing("rucksack");
    player.give(rucksack);
    rucksack.setLocation(player);
    rucksack.give(thing);
    thing.setLocation(rucksack);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You are already holding that",
                 e.getMessage());
  }

  @Test
  public void testFailureNotAPlainThing() throws Exception {
    command = new TakeCommand(actor, player, location);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You can only take ordinary things",
                 e.getMessage());
  }

  @Test
  public void testFailureUntakable() throws Exception {
    thing.getPolicy().setRoles(Category.GENERAL, Role.WIZARD_ROLES);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to take that",
                 e.getMessage());
  }

  @Test
  public void testFailureNoThingLocation() throws Exception {
    location.give(player);
    player.setLocation(location);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }

  @Test
  public void testFailureNoPlayerLocation() throws Exception {
    location.give(thing);
    thing.setLocation(location);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }

  @Test
  public void testSuccessSeize() throws Exception {
    Player holder = newTestPlayer("holder", Role.DENIZEN);
    holder.give(thing);
    thing.setLocation(holder);
    location.give(player);
    player.setLocation(location);

    setPlayerRoles(Role.ADEPT);

    assertTrue(command.execute());

    assertFalse(holder.has(thing));
    assertTrue(player.has(thing));
    assertEquals(player, thing.getLocation().get());
  }

  @Test
  public void testFailureSeize() throws Exception {
    Player holder = newTestPlayer("holder", Role.DENIZEN);
    holder.give(thing);
    thing.setLocation(holder);
    location.give(player);
    player.setLocation(location);

    setPlayerRoles(Role.BARD);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may not take something from another player",
                 e.getMessage());
  }

  @Test
  public void testSuccessWizardNotCollocated() throws Exception {
    location.give(thing);
    thing.setLocation(location);
    Place otherPlace = newTestPlace("otherPlace");
    otherPlace.give(player);
    player.setLocation(otherPlace);

    setPlayerRoles(Role.BARD);

    assertTrue(command.execute());

    assertFalse(location.has(thing));
    assertTrue(player.has(thing));
    assertEquals(player, thing.getLocation().get());
  }

  @Test
  public void testFailureNotCollocated() throws Exception {
    location.give(thing);
    thing.setLocation(location);
    Place otherPlace = newTestPlace("otherPlace");
    otherPlace.give(player);
    player.setLocation(otherPlace);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("You may only take that if you are in the same location",
                 e.getMessage());
  }
}
