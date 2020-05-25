package xyz.deszaras.grounds.command;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Thing;

public class IndexCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private IndexCommand command;

  @Before
  public void setUp() {
    super.setUp();

    command = new IndexCommand(actor, player, testUniverse);
  }

  @Test
  public void testSuccess() throws Exception {
    Thing thing1 = newTestThing("thing1");
    Thing thing2 = newTestThing("thing2");

    setPlayerRoles(Role.BARD);

    String index = command.execute();
    assertTrue(index.contains("thing1"));
    assertTrue(index.contains(thing1.getId().toString()));
    assertTrue(index.contains("thing2"));
    assertTrue(index.contains(thing2.getId().toString()));
  }

  @Test
  public void testFailure() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not a wizard in this universe, so you may not index it");

    command.execute();
  }
}
