package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CommandTest extends AbstractCommandTest {

  private class TestCommand extends Command<Boolean> {

    public TestCommand(Actor actor, Player player) {
      super(actor, player);
    }

    @Override
    protected Boolean executeImpl() {
      return true;
    }
  }

  private static class TestEvent extends Event<String> {
    private TestEvent() {
      super(Player.GOD, new Place("there"), "test");
    }
  }

  private Command command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    command = new TestCommand(actor, player);
  }

  @Test
  public void testEvents() {
    command.postEvents(Set.of(new TestEvent(), new TestEvent()));
    assertEquals(2, command.getEvents().size());
    command.postEvent(new TestEvent());
    assertEquals(3, command.getEvents().size());
  }

  // @Test
  // public void testCheckIfWizardSuccess() throws Exception {
  //   setPlayerRoles(Role.BARD);

  //   command.checkIfWizard("You are not a wizard");
  // }

  // @Test
  // public void testCheckIfWizardSuccessForGOD() throws Exception {
  //   command = new TestCommand(actor, Player.GOD);

  //   command.checkIfWizard("You are not a wizard");
  // }

  // @Test
  // public void testCheckIfWizardFailure() throws Exception {
  //   setPlayerRoles(Role.DENIZEN);

  //   PermissionException e = assertThrows(PermissionException.class,
  //                                        () -> command.checkIfWizard("You are not a wizard"));
  //   assertEquals("You are not a wizard", e.getMessage());
  // }

  @Test
  public void testCheckIfAnyRoleSuccess() throws Exception {
    setPlayerRoles(Role.DENIZEN, Role.ADEPT);

    assertTrue(command.checkIfAnyRole(Role.ADEPT, Role.THAUMATURGE));
    assertTrue(command.checkIfAnyRole(Set.of(Role.ADEPT, Role.THAUMATURGE)));
    command.checkIfAnyRole("Adept or greater, please",
                         Role.ADEPT, Role.THAUMATURGE);
  }

  @Test
  public void testCheckIfAnyRoleSuccessForGOD() throws Exception {
    command = new TestCommand(actor, Player.GOD);

    assertTrue(command.checkIfAnyRole(Role.ADEPT, Role.THAUMATURGE));
    assertTrue(command.checkIfAnyRole(Set.of(Role.ADEPT, Role.THAUMATURGE)));
    command.checkIfAnyRole("Adept or greater, please",
                         Role.ADEPT, Role.THAUMATURGE);
  }

  @Test
  public void testCheckIfAnyRoleFailure() throws Exception {
    setPlayerRoles(Role.DENIZEN, Role.BARD);

    assertFalse(command.checkIfAnyRole(Role.ADEPT, Role.THAUMATURGE));
    assertFalse(command.checkIfAnyRole(Set.of(Role.ADEPT, Role.THAUMATURGE)));

    PermissionException e = assertThrows(PermissionException.class,
        () -> command.checkIfAnyRole("Adept or higher", Role.ADEPT, Role.THAUMATURGE));
    assertEquals("Adept or higher", e.getMessage());
  }

  @Test
  public void testCheckPermissionSuccess() throws Exception {
    Thing thing = mock(Thing.class);
    when(thing.passes(Category.GENERAL, player)).thenReturn(true);

    command.checkPermission(Category.GENERAL, thing, "pass");
  }

  @Test
  public void testCheckPermissionFailure() throws Exception {
    Thing thing = mock(Thing.class);
    when(thing.passes(Category.GENERAL, player)).thenReturn(false);

    PermissionException e = assertThrows(PermissionException.class,
        () -> command.checkPermission(Category.GENERAL, thing, "fail"));
    assertTrue(e.getMessage().contains("fail"));
  }

}
