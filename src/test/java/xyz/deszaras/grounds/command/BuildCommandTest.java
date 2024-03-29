package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
// import xyz.deszaras.grounds.model.Extension;
// import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Place;
// import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class BuildCommandTest extends AbstractCommandTest {

  private Place place;
  private List<String> buildArgs;
  private BuildCommand command;

  @SuppressWarnings("PMD.EmptyCatchBlock")
  @BeforeEach
  public void setUp() {
    super.setUp();

    place = newTestPlace("building_place");
    place.give(player);
    player.setLocation(place);
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    buildArgs = List.of();
    command = new BuildCommand(actor, player, BuildCommand.BuiltInType.THING.name(),
                               "buildme", buildArgs);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not a bard or thaumaturge, so you may not build",
                 e.getMessage());
  }

  @Test
  public void testFailureDueToUnknownType() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    buildArgs = List.of();
    command = new BuildCommand(actor, player, "NOTATHING",
                               "buildme", buildArgs);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("I don't know how to build NOTATHING",
                 e.getMessage());
  }

  @Test
  public void testBuildThing() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    buildArgs = List.of();
    command = new BuildCommand(actor, player, BuildCommand.BuiltInType.THING.name(),
                               "buildme", buildArgs);

    String thingId = command.execute();

    Optional<Thing> builtOpt = testUniverse.getThingByName("buildme", Thing.class);
    assertTrue(builtOpt.isPresent());
    Thing built = builtOpt.get();

    assertEquals(thingId, built.getId().toString());

    assertTrue(testUniverse.getThing(built.getId()).isPresent());
    assertSame(built, testUniverse.getThing(built.getId()).get());

    assertTrue(built.getLocation().isPresent());
    assertEquals(place, built.getLocation().get());
    assertTrue(place.has(built));
  }

  // @Test
  // public void testDestroyThingAndSaveContents() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Thing thing = newTestThing("destroyme");
  //   thing.setLocation(place);
  //   place.give(thing);

  //   Thing mathom = newTestThing("mathom");
  //   thing.give(mathom);

  //   command = new DestroyCommand(actor, player, thing);

  //   assertTrue(command.execute());

  //   assertTrue(testUniverse.getLostAndFoundPlace().get().has(mathom));
  //   assertEquals(testUniverse.getLostAndFoundPlace().get(),
  //                mathom.getLocation().get());
  // }

  // @Test
  // public void testDestroyPlace() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Place place = newTestPlace("destroyme");

  //   command = new DestroyCommand(actor, player, place);

  //   assertTrue(command.execute());

  //   assertTrue(testUniverse.getThing(place.getId()).isEmpty());
  // }

  // @Test
  // public void testDestroyPlaceFailsWhenOccupied() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Place place = newTestPlace("destroyme");

  //   Thing thing = newTestThing("rock");
  //   place.give(thing);
  //   thing.setLocation(place);

  //   command = new DestroyCommand(actor, player, place);

  //   thrown.expect(CommandException.class);
  //   thrown.expectMessage("This place is occupied");
  //   command.execute();
  // }

  // @Test
  // public void testDestroyPlaceFailsWhenLinked() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Place place = newTestPlace("destroyme");
  //   Place place2 = newTestPlace("leavemealone");

  //   Link link = newTestLink("linky", place, place2);

  //   command = new DestroyCommand(actor, player, place);

  //   thrown.expect(CommandException.class);
  //   thrown.expectMessage("There are still links to this place");
  //   command.execute();
  // }

  // @Test
  // public void testDestroyLink() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Place place1 = newTestPlace("leavemealone");
  //   Place place2 = newTestPlace("leavemealone2");

  //   Link link = newTestLink("destroyme", place1, place2);

  //   command = new DestroyCommand(actor, player, link);

  //   assertTrue(command.execute());

  //   assertTrue(testUniverse.getThing(link.getId()).isEmpty());
  // }

  // @Test
  // public void testDestroyExtension() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Extension extension = newTestExtension("destroyme");

  //   command = new DestroyCommand(actor, player, extension);

  //   assertTrue(command.execute());

  //   assertTrue(testUniverse.getThing(extension.getId()).isEmpty());
  // }

  // @Test
  // public void testDestroyPlayer() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Player playerToDestroy = newTestPlayer("destroyme", Role.DENIZEN);
  //   playerToDestroy.setLocation(place);
  //   place.give(playerToDestroy);

  //   command = new DestroyCommand(actor, player, playerToDestroy);

  //   assertTrue(command.execute());

  //   assertTrue(testUniverse.getThing(playerToDestroy.getId()).isEmpty());
  //   assertFalse(place.has(playerToDestroy));
  //   assertTrue(testUniverse.getRoles(playerToDestroy).isEmpty());
  // }

  // @Test
  // public void testDestroyPlayerFailsWhenOccupied() throws Exception {
  //   setPlayerRoles(Role.THAUMATURGE);

  //   Player playerToDestroy = newTestPlayer("destroyme", Role.DENIZEN);
  //   playerToDestroy.setLocation(place);
  //   place.give(playerToDestroy);

  //   Actor actor2 = new Actor("bob");
  //   playerToDestroy.setCurrentActor(actor2);

  //   command = new DestroyCommand(actor, player, playerToDestroy);

  //   thrown.expect(CommandException.class);
  //   thrown.expectMessage("Someone is currently playing as that player");
  //   command.execute();
  // }
}
