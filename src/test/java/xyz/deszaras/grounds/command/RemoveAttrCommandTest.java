package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class RemoveAttrCommandTest extends AbstractCommandTest {

  private Thing thing;
  private RemoveAttrCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    thing = newTestThing("testThing");
    thing.setAttr("foo", "bar");
  }

  @Test
  public void testSuccess() throws Exception {
    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    command = new RemoveAttrCommand(actor, player, thing, "foo");
    assertTrue(command.execute());

    assertTrue(thing.getAttr("foo").isEmpty());
  }

  @Test
  public void testSuccessProtectedAttribute() throws Exception {
    thing.setAttr(AttrNames.NAME, "ok");

    command = new RemoveAttrCommand(actor, Player.GOD, thing, AttrNames.NAME);
    assertTrue(command.execute());

    assertTrue(thing.getAttr(AttrNames.NAME).isEmpty());
  }

  @Test
  public void testFailureProtectedAttribute() throws Exception {
    thing.setAttr(AttrNames.NAME, "noway");

    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    command = new RemoveAttrCommand(actor, player, thing, AttrNames.NAME);
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Only GOD may remove that attribute directly",
                 e.getMessage());
  }

  @Test
  public void testFailurePermission() throws Exception {
    testUniverse.addRole(Role.GUEST, player); // expect DEFAULT policy on thing

    command = new RemoveAttrCommand(actor, player, thing, "foo");
    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to remove attributes on this",
                 e.getMessage());
  }
}
