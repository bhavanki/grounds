package xyz.deszaras.grounds.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class RoleTest {

  private Universe universe;
  private Player player;

  @BeforeEach
  public void setUp() {
    universe = new Universe("test");
    Universe.setCurrent(universe);
    player = new Player("p1");
  }

  @Test
  public void testIsWizard() {
    assertFalse(Role.isWizard(player));

    universe.addRole(Role.DENIZEN, player);
    assertFalse(Role.isWizard(player));

    universe.addRole(Role.BARD, player);
    assertTrue(Role.isWizard(player));

    universe.removeRole(Role.DENIZEN, player);
    assertTrue(Role.isWizard(player));
  }

}
