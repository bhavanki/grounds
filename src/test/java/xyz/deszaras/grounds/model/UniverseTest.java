package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;

public class UniverseTest {

  private Universe u;
  private Player p;

  @BeforeEach
  public void setUp() {
    u = new Universe("test");
  }

  @Test
  public void testRoles() {
    p = new Player("bob", u);

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
