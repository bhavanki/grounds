package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class SetAttrCommandTest extends AbstractCommandTest {

  private Thing thing;
  private SetAttrCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.BARD);

    thing = newTestThing("testThing");
  }

  @Test
  public void testSuccess() throws Exception {
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
    command = new SetAttrCommand(actor,player, thing, new Attr(AttrNames.DESCRIPTION, "ok"));
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Only GOD may set that attribute directly",
                 e.getMessage());
  }

  @Test
  public void testFailurePermission() throws Exception {
    thing.getPolicy().setRoles(Category.WRITE, Set.of(Role.THAUMATURGE));

    command = new SetAttrCommand(actor, player, thing, new Attr("foo", "bar"));
    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to set attributes on this",
                 e.getMessage());
  }
}
