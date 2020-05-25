package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class CommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private class TestCommand extends Command<Boolean> {

    public TestCommand(Actor actor, Player player) {
      super(actor, player);
    }

    @Override
    public Boolean execute() {
      return true;
    }
  }

  private Actor actor;
  private Player player;
  private Command command;

  @Before
  public void setUp() {
    actor = mock(Actor.class);
    player = mock(Player.class);
    command = new TestCommand(actor, player);
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

    thrown.expect(PermissionException.class);
    thrown.expectMessage("fail");

    command.checkPermission(Category.GENERAL, thing, "fail");
  }

}
