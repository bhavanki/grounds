package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.ChangePolicyCommand.ChangeInstruction;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ChangePolicyCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Policy policy;
  private Thing thing;
  private ChangePolicyCommand command;

  @Before
  public void setUp() {
    super.setUp();

    policy = new Policy(Policy.DEFAULT);

    thing = mock(Thing.class, RETURNS_DEEP_STUBS);
    when(thing.getPolicy()).thenReturn(policy);
  }

  @Test
  public void testBasicAddRole() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Set<Role> roles = policy.getRoles(Policy.Category.USE);
    assertFalse(roles.contains(Role.GUEST));
    command = new ChangePolicyCommand(actor, player, thing, new ChangeInstruction("u+g"));
    command.execute();

    roles = policy.getRoles(Policy.Category.USE);
    assertTrue(roles.contains(Role.GUEST));
  }

  @Test
  public void testBasicRemoveRole() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    Set<Role> roles = policy.getRoles(Policy.Category.USE);
    assertTrue(roles.contains(Role.BARD));
    command = new ChangePolicyCommand(actor, player, thing, new ChangeInstruction("u-B"));
    command.execute();

    roles = policy.getRoles(Policy.Category.USE);
    assertFalse(roles.contains(Role.BARD));
  }

  @Test
  public void testAllCategories() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    for (Category c : Category.values()) {
      Set<Role> roles = policy.getRoles(c);
      assertFalse(roles.contains(Role.GUEST));
    }
    command = new ChangePolicyCommand(actor, player, thing,
                                      new ChangeInstruction("g+g,r+g,w+g,u+g"));
    command.execute();

    for (Category c : Category.values()) {
      Set<Role> roles = policy.getRoles(c);
      assertTrue(roles.contains(Role.GUEST));
    }
  }

  @Test
  public void testAllRoles() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    command = new ChangePolicyCommand(actor, player, thing,
                                      new ChangeInstruction("g-gdoBAT"));
    command.execute();

    assertTrue(policy.getRoles(Category.GENERAL).isEmpty());

    command = new ChangePolicyCommand(actor, player, thing,
                                      new ChangeInstruction("g+gdoBAT"));
    command.execute();

    assertTrue(policy.getRoles(Category.GENERAL).containsAll(Role.ALL_ROLES));
  }

  @Test
  public void testConflictingRoleFailure() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);

    thrown.expect(IllegalArgumentException.class);

    command = new ChangePolicyCommand(actor, player, thing,
                                      new ChangeInstruction("g+gd,g-Bg"));
    command.execute();
  }

  @Test
  public void testPermissionFailure() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not a thaumaturge in this universe, " +
                         "so you may not change policies on things");

    command = new ChangePolicyCommand(actor, player, thing, new ChangeInstruction("u+g"));
    command.execute();
  }

}
