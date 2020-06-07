package xyz.deszaras.grounds.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import xyz.deszaras.grounds.auth.Policy.Category;

public class PolicyTest {

  private Policy policy;
  private Set<Role> s1;
  private Set<Role> s2;

  @Before
  public void setUp() {
    policy = new Policy();
    s1 = ImmutableSet.of(Role.DENIZEN, Role.BARD);
    s2 = ImmutableSet.of(Role.DENIZEN, Role.ADEPT);
  }

  @Test
  public void testRoles() {
    policy.setRoles(Category.GENERAL, s1);
    assertEquals(s1, policy.getRoles(Category.GENERAL));

    policy.setRoles(Category.READ, s2);
    assertEquals(s1, policy.getRoles(Category.GENERAL));
    assertEquals(s2, policy.getRoles(Category.READ));

    assertTrue(policy.getRoles(Category.USE).isEmpty());
  }

  @Test
  public void testCopyConstructor() {
    Policy policy2 = new Policy(policy);
    assertEquals(policy.getPermissions(), policy2.getPermissions());
  }

  @Test
  public void testCategories() {
    policy.setRoles(Category.GENERAL, s1);
    policy.setRoles(Category.READ, s2);

    assertEquals(ImmutableSet.of(Category.GENERAL, Category.READ),
                 policy.getCategories(Role.DENIZEN));
    assertEquals(ImmutableSet.of(Category.GENERAL),
                 policy.getCategories(Role.BARD));
    assertEquals(ImmutableSet.of(Category.READ),
                 policy.getCategories(Role.ADEPT));
    assertEquals(ImmutableSet.of(),
                 policy.getCategories(Role.OWNER));
  }

  @Test
  public void testPasses() {
    policy.setRoles(Category.GENERAL, s1);
    policy.setRoles(Category.READ, s2);

    assertTrue(policy.passes(Category.GENERAL, s1));
    assertTrue(policy.passes(Category.READ, s2));

    Set<Role> s = ImmutableSet.of(Role.BARD);
    assertTrue(policy.passes(Category.GENERAL, s));
    assertFalse(policy.passes(Category.READ, s));

    s = ImmutableSet.of(Role.OWNER);
    assertFalse(policy.passes(Category.GENERAL, s));
    assertFalse(policy.passes(Category.READ, s));
  }

}
