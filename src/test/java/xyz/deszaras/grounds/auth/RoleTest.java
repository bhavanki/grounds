package xyz.deszaras.grounds.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

import org.junit.Before;
import org.junit.Test;

public class RoleTest {

  private Universe universe;
  private Player player;

  @Before
  public void setUp() {
    universe = new Universe("test");
    player = new Player("p1", universe);
  }

  @Test
  public void testIsWizard() {
    assertFalse(Role.isWizard(player, universe));

    universe.addRole(Role.DENIZEN, player);
    assertFalse(Role.isWizard(player, universe));

    universe.addRole(Role.BARD, player);
    assertTrue(Role.isWizard(player, universe));

    universe.removeRole(Role.DENIZEN, player);
    assertTrue(Role.isWizard(player, universe));
  }

}
