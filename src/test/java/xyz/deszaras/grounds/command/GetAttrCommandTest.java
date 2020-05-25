package xyz.deszaras.grounds.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Thing;

public class GetAttrCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Attr attr;

  private Thing thing;
  private GetAttrCommand command;

  @Before
  public void setUp() {
    super.setUp();

    attr = new Attr("foo", "bar");

    thing = newTestThing("testThing");
    command = new GetAttrCommand(actor, player, thing, "foo");
  }

  @Test
  public void testSuccess() throws Exception {
    thing.setAttr(attr);
    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    assertEquals(attr.toAttrSpec(), command.execute());
  }

  @Test
  public void testFailureAttributeMissing() throws Exception {
    testUniverse.addRole(Role.BARD, player); // expect DEFAULT policy on thing

    thrown.expect(CommandException.class);
    thrown.expectMessage("There is no attribute named foo on this");

    command.execute();
  }

  @Test
  public void testFailurePermission() throws Exception {
    thing.setAttr(attr);
    testUniverse.addRole(Role.GUEST, player); // expect DEFAULT policy on thing

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to get attributes on this");

    command.execute();
  }
}
