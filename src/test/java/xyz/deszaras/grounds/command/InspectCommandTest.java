package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Thing;

public class InspectCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Thing thing;
  private InspectCommand command;

  @Before
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

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to inspect this");

    command.execute();
  }
}
