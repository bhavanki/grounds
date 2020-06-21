package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

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
  public void testBasicAddAndGetThings() {
    Collection<Thing> allThings = u.getThings();
    assertTrue(allThings.isEmpty());

    Thing t1 = new Thing("item1");
    u.addThing(t1);

    assertEquals(t1, u.getThing(t1.getId()).get());
    assertEquals(t1, u.getThing(t1.getId().toString()).get());
    allThings = u.getThings();
    assertEquals(1, allThings.size());
    assertTrue(allThings.contains(t1));

    Thing t2 = new Thing("item2");
    u.addThing(t2);

    assertEquals(t2, u.getThing(t2.getId()).get());
    assertEquals(t2, u.getThing(t2.getId().toString()).get());
    assertEquals(t1, u.getThing(t1.getId()).get());
    assertEquals(t1, u.getThing(t1.getId().toString()).get());
    allThings = u.getThings();
    assertEquals(2, allThings.size());
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
    assertEquals(1, allThings.size());
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

}
