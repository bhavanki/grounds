package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * A helpful base class for command tests.
 */
public abstract class AbstractCommandTest {

  protected Universe testUniverse;

  protected Actor actor;
  protected Player player;

  /**
   * Sets up useful things for a command test.<p>
   * <ul>
   * <li>A test universe is created, with a lost and found place.</li>
   * <li>An actor and player are mocked.</li>
   * <li>The mock player gets a random ID and is joined to the test universe.</li>
   * </ul>
   */
  public void setUp() {
    testUniverse = new Universe("test");
    Multiverse.MULTIVERSE.putUniverse(testUniverse);

    Place laf = new Place("LOST+FOUND", testUniverse);
    testUniverse.addThing(laf);
    testUniverse.setLostAndFoundId(laf.getId());

    actor = mock(Actor.class);
    player = mock(Player.class);
    when(player.getId()).thenReturn(UUID.randomUUID());
    when(player.getUniverse()).thenReturn(testUniverse);
  }

  /**
   * Gets the test universe.
   *
   * @return test universe
   */
  public Universe getTestUniverse() {
    return testUniverse;
  }

  /**
   * Creates a new test extension.
   *
   * @param  name extension name
   * @return      new extension
   */
  protected Extension newTestExtension(String name) {
    Extension extension = new Extension(name, testUniverse);
    testUniverse.addThing(extension);
    extension.setUniverse(testUniverse);
    return extension;
  }

  /**
   * Creates a new test link.
   *
   * @param  name   link name
   * @param  place1 source place to link
   * @param  place2 destination place to link
   * @return      new link
   */
  protected Link newTestLink(String name, Place place1, Place place2) {
    Link link = new Link(name, testUniverse, place1, "source", place2,
                         "destination");
    testUniverse.addThing(link);
    link.setUniverse(testUniverse);
    return link;
  }

  /**
   * Creates a new test place.
   *
   * @param  name place name
   * @return      new place
   */
  protected Place newTestPlace(String name) {
    Place place = new Place(name, testUniverse);
    testUniverse.addThing(place);
    place.setUniverse(testUniverse);
    return place;
  }

  /**
   * Creates a new test player.
   *
   * @param  name player name
   * @return      new player
   */
  protected Player newTestPlayer(String name, Role role) {
    Player player = new Player(name, testUniverse);
    testUniverse.addThing(player);
    player.setUniverse(testUniverse);
    testUniverse.addRole(role, player);
    return player;
  }

  /**
   * Creates a new test thing. Usually a mock is better, but Mockito cannot
   * mock final methods such as those working with attributes, so a real thing
   * is necessary for those tests.
   *
   * @param  name thing name
   * @return      new thing
   */
  protected Thing newTestThing(String name) {
    Thing thing = new Thing(name, testUniverse);
    testUniverse.addThing(thing);
    thing.setUniverse(testUniverse);
    return thing;
  }

  /**
   * Sets the roles for the mock player in the test universe. Normally this is
   * not established.
   *
   * @param roles roles to set
   */
  protected void setPlayerRoles(Role... roles) {
    for (Role role : roles) {
      testUniverse.addRole(role, player);
    }
  }
}
