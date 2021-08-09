package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ThingTest {

  private Universe universe;
  private Thing thing;

  @BeforeEach
  public void setUp() {
    universe = new Universe("test");
    Universe.setCurrent(universe);
    thing = new Thing("doojob");
  }

  @Test
  public void testGetId() {
    assertNotNull(thing.getId());
  }

  @Test
  public void testGetName() {
    assertEquals("doojob", thing.getName());
  }

  @Test
  public void testDescription() {
    assertTrue(thing.getDescription().isEmpty());

    thing.setDescription("Not sure");
    assertEquals("Not sure", thing.getDescription().get());

    thing.setDescription(null);
    assertTrue(thing.getDescription().isEmpty());
  }

  @Test
  public void testLocation() throws Exception {
    assertTrue(thing.getLocation().isEmpty());

    Place location = new Place("workshop");
    universe.addThing(location);

    thing.setLocation(location);
    assertEquals(location, thing.getLocation().get());

    Place nowhere = new Place("nowhere");
    thing.setLocation(nowhere);
    assertThrows(MissingThingException.class, () -> thing.getLocation());

    thing.setLocation(null);
    assertTrue(thing.getLocation().isEmpty());
  }

  @Test
  public void testOwner() throws Exception {
    assertTrue(thing.getOwner().isEmpty());

    Player owner = new Player("manny");
    universe.addThing(owner);

    thing.setOwner(owner);
    assertEquals(owner, thing.getOwner().get());

    Player nobody = new Player("nobody");
    thing.setOwner(nobody);
    assertThrows(MissingThingException.class, () -> thing.getOwner());

    thing.setOwner(null);
    assertTrue(thing.getOwner().isEmpty());
  }

  @Test
  public void testHome() throws Exception {
    assertTrue(thing.getHome().isEmpty());

    Place home = new Place("toolbox");
    universe.addThing(home);

    thing.setHome(home);
    assertEquals(home, thing.getHome().get());

    Place nowhere = new Place("nowhere");
    thing.setHome(nowhere);
    assertThrows(MissingThingException.class, () -> thing.getHome());

    thing.setHome(null);
    assertTrue(thing.getHome().isEmpty());
  }

  @Test
  public void testMuteList() {
    assertTrue(thing.getMuteList().isEmpty());

    Player player1 = new Player("annoying1");
    universe.addThing(player1);
    Player player2 = new Player("annoying2");
    universe.addThing(player2);
    Player player3 = new Player("normal");
    universe.addThing(player3);

    thing.setMuteList(List.of(player1, player2));
    assertEquals(List.of(player1, player2), thing.getMuteList());
    assertTrue(thing.mutes(player1));
    assertTrue(thing.mutes(player2));
    assertFalse(thing.mutes(player3));

    Player player4 = new Player("ghost");
    thing.setMuteList(List.of(player1, player4));
    assertEquals(List.of(player1), thing.getMuteList());
    assertTrue(thing.mutes(player1));
    // assertTrue(thing.mutes(player4));

    thing.setMuteList(null);
    assertTrue(thing.getMuteList().isEmpty());
  }

  @Test
  public void testAttrStringValue() {
    thing.setAttr("a", "b");
    Optional<Attr> a = thing.getAttr("a", Attr.Type.STRING);
    assertTrue(a.isPresent());
    assertEquals("b", a.get().getValue());
  }

  @Test
  public void testAttrIntValue() {
    thing.setAttr("a", 42);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.INTEGER);
    assertTrue(a.isPresent());
    assertEquals(42, a.get().getIntValue());
  }

  @Test
  public void testAttrBooleanValue() {
    thing.setAttr("a", true);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.BOOLEAN);
    assertTrue(a.isPresent());
    assertEquals(true, a.get().getBooleanValue());
  }

  @Test
  public void testAttrInstantValue() {
    Instant ts = Instant.ofEpochSecond(123456L);
    thing.setAttr("a", ts);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.TIMESTAMP);
    assertTrue(a.isPresent());
    assertEquals(ts, a.get().getInstantValue());
  }

  @Test
  public void testAttrThingValue() {
    Thing aThing = new Thing("doohickey");
    thing.setAttr("a", aThing);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.THING);
    assertTrue(a.isPresent());
    assertEquals(aThing.getId().toString(), a.get().getThingValue());
  }

  @Test
  public void testAttrAttrValue() {
    Attr nested = new Attr("n", "v");
    thing.setAttr("a", nested);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.ATTR);
    assertTrue(a.isPresent());
    assertEquals(nested, a.get().getAttrValue());
  }

  @Test
  public void testAttrAttrListValue() {
    Attr nested1 = new Attr("n1", "v1");
    Attr nested2 = new Attr("n2", "v2");
    List<Attr> nested = List.of(nested1, nested2);
    thing.setAttr("a", nested);
    Optional<Attr> a = thing.getAttr("a", Attr.Type.ATTRLIST);
    assertTrue(a.isPresent());
    assertEquals(nested, a.get().getAttrListValue());
  }
}
