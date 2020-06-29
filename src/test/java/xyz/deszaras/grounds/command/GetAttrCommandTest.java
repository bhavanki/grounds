package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Thing;

public class GetAttrCommandTest extends AbstractCommandTest {

  private Attr attr;

  private Thing thing;
  private GetAttrCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    attr = new Attr("foo", "bar");

    thing = newTestThing("testThing");
    command = new GetAttrCommand(actor, player, thing, "foo");
  }

  @Test
  public void testSuccess() throws Exception {
    thing.setAttr(attr);

    assertEquals(attr.toAttrSpec(), command.execute());
  }

  @Test
  public void testFailureAttributeMissing() throws Exception {
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("There is no attribute named foo on this",
                 e.getMessage());
  }

  @Test
  public void testFailurePermission() throws Exception {
    thing.setAttr(attr);
    thing.getPolicy().setRoles(Category.READ, Set.of(Role.BARD));

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to get attributes on this",
                 e.getMessage());
  }
}
