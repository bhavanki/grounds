package xyz.deszaras.grounds.command;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class RemoveAttrCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Thing thing;
  private RemoveAttrCommand command;

  @Before
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

    thrown.expect(CommandException.class);
    thrown.expectMessage("Only GOD may remove that attribute directly");

    command = new RemoveAttrCommand(actor, player, thing, AttrNames.NAME);
    command.execute();
  }

  @Test
  public void testFailurePermission() throws Exception {
    testUniverse.addRole(Role.GUEST, player); // expect DEFAULT policy on thing

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to remove attributes on this");

    command = new RemoveAttrCommand(actor, player, thing, "foo");
    command.execute();
  }
}
