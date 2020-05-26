package xyz.deszaras.grounds.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class SetAttrCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Thing thing;
  private SetAttrCommand command;

  @Before
  public void setUp() {
    super.setUp();

    thing = newTestThing("testThing");
  }

  @Test
  public void testSuccess() throws Exception {
    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    command = new SetAttrCommand(actor, player, thing, new Attr("foo", "bar"));
    assertTrue(command.execute());

    assertTrue(thing.getAttr("foo").isPresent());
    assertEquals("bar", thing.getAttr("foo").get().getValue());
  }

  @Test
  public void testSuccessProtectedAttribute() throws Exception {
    command = new SetAttrCommand(actor, Player.GOD, thing, new Attr(AttrNames.DESCRIPTION, "ok"));
    assertTrue(command.execute());

    assertTrue(thing.getAttr(AttrNames.DESCRIPTION).isPresent());
    assertEquals("ok", thing.getAttr(AttrNames.DESCRIPTION).get().getValue());
  }

  @Test
  public void testFailureProtectedAttribute() throws Exception {
    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    thrown.expect(CommandException.class);
    thrown.expectMessage("Only GOD may set that attribute directly");

    command = new SetAttrCommand(actor,player, thing, new Attr(AttrNames.DESCRIPTION, "ok"));
    command.execute();
  }

  @Test
  public void testFailurePermission() throws Exception {
    testUniverse.addRole(Role.GUEST, player); // expect DEFAULT policy on thing

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to set attributes on this");

    command = new SetAttrCommand(actor, player, thing, new Attr("foo", "bar"));
    command.execute();
  }
}