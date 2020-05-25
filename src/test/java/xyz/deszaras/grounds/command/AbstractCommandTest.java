package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Multiverse;
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
   * <li>A test universe is created.</li>
   * <li>An actor and player are mocked.</li>
   * <li>The mock player gets a random ID and is joined to the test universe.</li>
   * </ul>
   */
  public void setUp() {
    testUniverse = new Universe("test");
    Multiverse.MULTIVERSE.putUniverse(testUniverse);

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
