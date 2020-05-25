package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Universe;

@SuppressWarnings("PMD.TooManyStaticImports")
public class TeleportCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Place source;
  private Place destination;
  private Universe destinationUniverse;
  private TeleportCommand command;
  private LookCommand testLookCommand;

  @Before
  public void setUp() {
    super.setUp();

    source = mock(Place.class);

    destination = mock(Place.class);
    destinationUniverse = mock(Universe.class);
    when(destination.getUniverse()).thenReturn(destinationUniverse);

    command = new TeleportCommand(actor, player, destination);

    testLookCommand = mock(LookCommand.class);
    try {
      when(testLookCommand.execute()).thenReturn("here");
      command.setTestLookCommand(testLookCommand);
    } catch (CommandException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.getLocation()).thenReturn(Optional.of(source));
    when(destination.passes(Category.GENERAL, player)).thenReturn(true);

    assertEquals("here", command.execute());

    verify(source).take(player);
    verify(destination).give(player);

    assertTrue(getTestUniverse().getThing(player.getId()).isEmpty());
    verify(destinationUniverse).addThing(player);

    verify(player).setLocation(destination);
  }

  @Test
  public void testSuccessNoSource() throws Exception {
    when(player.getLocation()).thenReturn(Optional.empty());
    when(destination.passes(Category.GENERAL, player)).thenReturn(true);

    assertEquals("here", command.execute());

    verify(destination).give(player);

    assertTrue(getTestUniverse().getThing(player.getId()).isEmpty());
    verify(destinationUniverse).addThing(player);

    verify(player).setLocation(destination);
  }

  @Test
  public void testFailureNotPermitted() throws Exception {
    when(player.getLocation()).thenReturn(Optional.of(source));
    when(destination.passes(Category.GENERAL, player)).thenReturn(false);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to move there");

    command.execute();
  }
}
