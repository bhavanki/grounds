package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
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
   * <li>An actor and player are created.</li>
   * <li>The player is joined to the test universe.</li>
   * </ul>
   */
  public void setUp() {
    testUniverse = new Universe("test");
    Universe.setCurrent(testUniverse);

    actor = new Actor("actor1");
    player = new Player("player");
    testUniverse.addThing(player);
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
    Extension extension = new Extension(name);
    testUniverse.addThing(extension);
    return extension;
  }

  /**
   * Creates a new test link.
   *
   * @param  name       link name
   * @param  place1     source place to link
   * @param  place1Name name of exit leading to source
   * @param  place2     destination place to link
   * @param  place2Name name of exit leading to destination
   * @return      new link
   */
  protected Link newTestLink(String name, Place place1, String place1Name,
                             Place place2, String place2Name) {
    Link link = new Link(name, place1, place1Name, place2, place2Name);
    testUniverse.addThing(link);
    return link;
  }

  /**
   * Creates a new test place.
   *
   * @param  name place name
   * @return      new place
   */
  protected Place newTestPlace(String name) {
    Place place = new Place(name);
    testUniverse.addThing(place);
    return place;
  }

  /**
   * Creates a new test player.
   *
   * @param  name player name
   * @return      new player
   */
  protected Player newTestPlayer(String name, Role role) {
    Player player = new Player(name);
    testUniverse.addThing(player);
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
    Thing thing = new Thing(name);
    testUniverse.addThing(thing);
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

  protected <T extends Event> T verifyEvent(T expectedEvent, Command command) {
    Optional<T> actualEventOpt =
        command.getEvents().stream()
            .filter(e -> e.getClass().equals(expectedEvent.getClass()))
            .findFirst();
    if (actualEventOpt.isEmpty()) {
      fail("Expected event of type " + expectedEvent.getClass() + " not found");
    }
    T actualEvent = actualEventOpt.get();
    assertEquals(expectedEvent.getPlayer(), actualEvent.getPlayer());
    assertEquals(expectedEvent.getLocation(), actualEvent.getLocation());
    return actualEvent;
  }
}
