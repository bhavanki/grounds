package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Place;

public class LookCommandTest extends AbstractCommandTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Place location;
  private LookCommand command;

  @Before
  public void setUp() {
    super.setUp();

    location = mock(Place.class);
    when(location.getName()).thenReturn("somewhere");
    when(location.getDescription()).thenReturn(Optional.empty());
    when(location.getContents()).thenReturn(Set.of());

    command = new LookCommand(actor, player);
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.getLocation()).thenReturn(Optional.of(location));
    when(location.passes(Category.READ, player)).thenReturn(true);

    String lookResult = command.execute();

    assertTrue(lookResult.contains("somewhere"));
  }

  @Test
  public void testFailureCannotLook() throws Exception {
    when(player.getLocation()).thenReturn(Optional.of(location));
    when(location.passes(Category.READ, player)).thenReturn(false);

    thrown.expect(PermissionException.class);
    thrown.expectMessage("You are not permitted to look at where you are");

    command.execute();
  }

  @Test
  public void testSuccessNowhere() throws Exception {
    when(player.getLocation()).thenReturn(Optional.empty());

    String lookResult = command.execute();

    assertTrue(lookResult.contains("nowhere"));
  }
}
