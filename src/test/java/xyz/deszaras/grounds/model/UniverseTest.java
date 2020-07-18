package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import xyz.deszaras.grounds.auth.Role;

@SuppressWarnings("PMD.TooManyStaticImports")
public class UniverseTest {

  private Universe u;
  private Player p;

  @BeforeEach
  public void setUp() {
    u = new Universe("test");
    Universe.setCurrent(u);
  }

  @Test
  public void testGetCurrent() {
    assertEquals(u, Universe.getCurrent());
  }

  @Test
  public void testGetName() {
    assertEquals("test", u.getName());
  }

  @Test
  public void testOrigin() {
    Place origin = u.getOriginPlace();
    assertNotNull(origin);
    assertEquals(origin.getId(), u.getOriginId());
    assertEquals(origin, u.getThing(origin.getId(), Place.class).get());

    Place newOrigin = new Place("NEW ORIGIN");
    u.addThing(newOrigin);
    u.setOrigin(newOrigin);

    assertEquals(newOrigin, u.getOriginPlace());
    assertEquals(newOrigin.getId(), u.getOriginId());
  }

  @Test
  public void testLostAndFound() {
    Place lostAndFound = u.getLostAndFoundPlace();
    assertNotNull(lostAndFound);
    assertEquals(lostAndFound.getId(), u.getLostAndFoundId());
    assertEquals(lostAndFound, u.getThing(lostAndFound.getId(), Place.class).get());

    Place newLostAndFound = new Place("NEW LOST+FOUND");
    u.addThing(newLostAndFound);
    u.setLostAndFound(newLostAndFound);

    assertEquals(newLostAndFound, u.getLostAndFoundPlace());
    assertEquals(newLostAndFound.getId(), u.getLostAndFoundId());
  }

  @Test
  public void testGuestHome() {
    Place guestHome = u.getGuestHomePlace();
    assertNotNull(guestHome);
    assertEquals(guestHome.getId(), u.getGuestHomeId());
    assertEquals(guestHome, u.getThing(guestHome.getId(), Place.class).get());

    Place newGuestHome = new Place("NEW GUEST HOME");
    u.addThing(newGuestHome);
    u.setGuestHome(newGuestHome);

    assertEquals(newGuestHome, u.getGuestHomePlace());
    assertEquals(newGuestHome.getId(), u.getGuestHomeId());
  }

  @Test
  public void testBasicAddAndGetThings() {
    Collection<Thing> allThings = u.getThings();
    assertEquals(3, allThings.size());

    Thing t1 = new Thing("item1");
    u.addThing(t1);

    assertEquals(t1, u.getThing(t1.getId()).get());
    assertEquals(t1, u.getThing(t1.getId().toString()).get());
    allThings = u.getThings();
    assertEquals(1 + 3, allThings.size());
    assertTrue(allThings.contains(t1));

    Thing t2 = new Thing("item2");
    u.addThing(t2);

    assertEquals(t2, u.getThing(t2.getId()).get());
    assertEquals(t2, u.getThing(t2.getId().toString()).get());
    assertEquals(t1, u.getThing(t1.getId()).get());
    assertEquals(t1, u.getThing(t1.getId().toString()).get());
    allThings = u.getThings();
    assertEquals(2 + 3, allThings.size());
    assertTrue(allThings.contains(t1));
    assertTrue(allThings.contains(t2));
  }

  @Test
  public void testRemoveThing() {
    Thing t1 = new Thing("item1");
    u.addThing(t1);
    Thing t2 = new Thing("item2");
    u.addThing(t2);

    u.removeThing(t1);

    Collection<Thing> allThings = u.getThings();
    assertEquals(1 + 3, allThings.size());
    assertTrue(allThings.contains(t2));
  }

  @Test
  public void testGetThingByType() {
    Place p = new Place("here");
    u.addThing(p);
    Thing t = new Thing("item");
    u.addThing(t);

    assertEquals(p, u.getThing(p.getId(), Place.class).get());
    assertEquals(p, u.getThing(p.getId().toString(), Place.class).get());

    assertEquals(t, u.getThing(t.getId(), Thing.class).get());
    assertEquals(t, u.getThing(t.getId().toString(), Thing.class).get());

    // OK to use superclass
    assertEquals(p, u.getThing(p.getId(), Thing.class).get());
    assertEquals(p, u.getThing(p.getId().toString(), Thing.class).get());
  }

  @Test
  public void testGetThingByWrongType() {
    Thing t = new Thing("item");
    u.addThing(t);

    assertThrows(IllegalArgumentException.class,
                 () -> u.getThing(t.getId(), Place.class));
    assertThrows(IllegalArgumentException.class,
                 () -> u.getThing(t.getId().toString(), Place.class));
  }

  @Test
  public void testGetThingByName() {
    Place p = new Place("here");
    u.addThing(p);
    Thing t = new Thing("item");
    u.addThing(t);

    assertEquals(p, u.getThingByName("here", Place.class).get());

    assertEquals(t, u.getThingByName("item", Thing.class).get());

    // OK to use superclass
    assertEquals(p, u.getThingByName("here", Thing.class).get());
  }

  @Test
  public void testGetThingByNameButWrongType() {
    Thing t = new Thing("item");
    u.addThing(t);

    assertThrows(IllegalArgumentException.class,
                 () -> u.getThing("item", Place.class));
  }

  @Test
  public void testRoles() {
    p = new Player("bob");

    assertEquals(0, u.getRoles(p).size());

    Set<Role> currentRoles;

    currentRoles = u.addRole(Role.BARD, p);
    assertEquals(1, currentRoles.size());
    assertTrue(currentRoles.contains(Role.BARD));
    assertEquals(currentRoles, u.getRoles(p));

    currentRoles = u.addRole(Role.ADEPT, p);
    assertEquals(2, currentRoles.size());
    assertTrue(currentRoles.contains(Role.BARD));
    assertTrue(currentRoles.contains(Role.ADEPT));
    assertEquals(currentRoles, u.getRoles(p));

    currentRoles = u.removeRole(Role.BARD, p);
    assertEquals(1, currentRoles.size());
    assertTrue(currentRoles.contains(Role.ADEPT));
    assertEquals(currentRoles, u.getRoles(p));
  }

  @Test
  public void testLoadAndSave(@TempDir Path tempDir) throws Exception {
    Thing t1 = new Thing("item1");
    u.addThing(t1);
    Thing t2 = new Thing("item2");
    u.addThing(t2);

    File saveFile = tempDir.resolve("universe.json").toFile();

    Universe.save(u, saveFile);
    Universe u2 = Universe.load(saveFile);

    Collection<Thing> allThings = u2.getThings();
    assertEquals(2 + 3, allThings.size());
    assertTrue(allThings.contains(t1));
    assertTrue(allThings.contains(t2));
  }

  @Test
  public void testSaveCurrent(@TempDir Path tempDir) throws Exception {
    testSaveCurrent(tempDir, true);
  }

  @Test
  public void testSaveCurrentUnsafe(@TempDir Path tempDir) throws Exception {
    testSaveCurrent(tempDir, false);
  }

  private void testSaveCurrent(Path tempDir, boolean safe) throws Exception {
    Thing t1 = new Thing("item1");
    u.addThing(t1);

    File saveFile = tempDir.resolve("universe.json").toFile();

    Universe.setCurrentFile(saveFile);
    assertTrue(Universe.saveCurrent(safe));

    Universe u2 = Universe.load(saveFile);
    Collection<Thing> allThings = u2.getThings();
    assertEquals(1 + 3, allThings.size());
    assertTrue(allThings.contains(t1));
  }

  @Test
  public void testSaveCurrentNoCurrentUniverse() throws Exception {
    Universe.setCurrent(null);
    assertFalse(Universe.saveCurrent(false));
  }

  @Test
  public void testSaveCurrentVoidUniverse() throws Exception {
    Universe.setCurrent(Universe.VOID);
    assertFalse(Universe.saveCurrent(false));
  }

  @Test
  public void testSaveCurrentNoCurrentUniverseFile() throws Exception {
    Universe.setCurrentFile(null);
    assertFalse(Universe.saveCurrent(false));
  }

  @Test
  public void testRemoveGuests() {
    Place p = new Place("here");
    u.addThing(p);

    Player p1 = new Player("bob");
    u.addRole(Role.DENIZEN, p1);
    u.addThing(p1);
    p1.setLocation(p);
    p.give(p1);

    Player p2 = new Player("guest1");
    u.addRole(Role.GUEST, p2);
    u.addThing(p2);
    p2.setLocation(p);
    p.give(p2);

    assertEquals(Set.of(p1, p2), u.getThings(Player.class));

    u.removeGuests();

    assertEquals(Set.of(p1), u.getThings(Player.class));
    assertEquals(Set.of(p1.getId()), p.getContents());
  }
}
