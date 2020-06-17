package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class DestroyCommandTest extends AbstractCommandTest {

  private Place place;
  private DestroyCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    place = newTestPlace("destruction_place");
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    Thing thing = newTestThing("destroyme");
    command = new DestroyCommand(actor, player, thing);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not a wizard, so you may not destroy", e.getMessage());
  }

  @Test
  public void testDestroyThing() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Thing thing = newTestThing("destroyme");
    thing.setLocation(place);
    place.give(thing);

    command = new DestroyCommand(actor, player, thing);

    assertTrue(command.execute());

    assertTrue(testUniverse.getThing(thing.getId()).isEmpty());
    assertFalse(place.has(thing));
  }

  @Test
  public void testDestroyThingAndSaveContents() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Thing thing = newTestThing("destroyme");
    thing.setLocation(place);
    place.give(thing);

    Thing mathom = newTestThing("mathom");
    thing.give(mathom);

    command = new DestroyCommand(actor, player, thing);

    assertTrue(command.execute());

    assertTrue(testUniverse.getLostAndFoundPlace().get().has(mathom));
    assertEquals(testUniverse.getLostAndFoundPlace().get(),
                 mathom.getLocation().get());
  }

  @Test
  public void testDestroyPlace() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Place place = newTestPlace("destroyme");

    command = new DestroyCommand(actor, player, place);

    assertTrue(command.execute());

    assertTrue(testUniverse.getThing(place.getId()).isEmpty());
  }

  @Test
  public void testDestroyPlaceFailsWhenOccupied() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Place place = newTestPlace("destroyme");

    Thing thing = newTestThing("rock");
    place.give(thing);
    thing.setLocation(place);

    command = new DestroyCommand(actor, player, place);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("This place is occupied"));
  }

  @Test
  public void testDestroyPlaceFailsWhenLinked() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Place place = newTestPlace("destroyme");
    Place place2 = newTestPlace("leavemealone");

    newTestLink("linky", place, place2);

    command = new DestroyCommand(actor, player, place);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("There are still links to this place"));
  }

  @Test
  public void testDestroyLink() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Place place1 = newTestPlace("leavemealone");
    Place place2 = newTestPlace("leavemealone2");

    Link link = newTestLink("destroyme", place1, place2);

    command = new DestroyCommand(actor, player, link);

    assertTrue(command.execute());

    assertTrue(testUniverse.getThing(link.getId()).isEmpty());
  }

  @Test
  public void testDestroyExtension() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Extension extension = newTestExtension("destroyme");

    command = new DestroyCommand(actor, player, extension);

    assertTrue(command.execute());

    assertTrue(testUniverse.getThing(extension.getId()).isEmpty());
  }

  @Test
  public void testDestroyPlayer() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Player playerToDestroy = newTestPlayer("destroyme", Role.DENIZEN);
    playerToDestroy.setLocation(place);
    place.give(playerToDestroy);

    command = new DestroyCommand(actor, player, playerToDestroy);

    assertTrue(command.execute());

    assertTrue(testUniverse.getThing(playerToDestroy.getId()).isEmpty());
    assertFalse(place.has(playerToDestroy));
    assertTrue(testUniverse.getRoles(playerToDestroy).isEmpty());
  }

  @Test
  public void testDestroyPlayerFailsWhenOccupied() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Player playerToDestroy = newTestPlayer("destroyme", Role.DENIZEN);
    playerToDestroy.setLocation(place);
    place.give(playerToDestroy);

    Actor actor2 = new Actor("bob");
    playerToDestroy.setCurrentActor(actor2);

    command = new DestroyCommand(actor, player, playerToDestroy);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("Someone is currently playing as that player"));
  }
}
