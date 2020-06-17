package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class LookCommandTest extends AbstractCommandTest {

  private Place location;
  private LookCommand command;

  @BeforeEach
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

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to look at where you are",
                 e.getMessage());
  }

  @Test
  public void testSuccessNowhere() throws Exception {
    when(player.getLocation()).thenReturn(Optional.empty());

    String lookResult = command.execute();

    assertTrue(lookResult.contains("nowhere"));
  }
}
