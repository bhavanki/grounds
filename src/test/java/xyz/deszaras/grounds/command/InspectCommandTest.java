package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Thing;

public class InspectCommandTest extends AbstractCommandTest {

  private Thing thing;
  private InspectCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    thing = mock(Thing.class);
    when(thing.toJson()).thenReturn("{}");
    command = new InspectCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    when(thing.passes(Category.WRITE, player)).thenReturn(true);

    assertEquals("{}", command.execute());
  }

  @Test
  public void testFailureUninspectable() throws Exception {
    when(thing.passes(Category.WRITE, player)).thenReturn(false);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to inspect this", e.getMessage());
  }
}
