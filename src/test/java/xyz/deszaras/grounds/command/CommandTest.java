package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class CommandTest {

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

  @BeforeEach
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

    PermissionException e = assertThrows(PermissionException.class,
        () -> command.checkPermission(Category.GENERAL, thing, "fail"));
    assertTrue(e.getMessage().contains("fail"));
  }

}
