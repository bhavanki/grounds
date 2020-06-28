package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Thing;

public class IndexCommandTest extends AbstractCommandTest {

  private IndexCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    command = new IndexCommand(actor, player);
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

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not a wizard, so you may not index the universe",
                 e.getMessage());
  }
}
