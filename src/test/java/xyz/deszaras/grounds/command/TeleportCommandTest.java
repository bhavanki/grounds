package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class TeleportCommandTest extends AbstractCommandTest {

  private Place source;
  private Place destination;
  private TeleportCommand command;
  private LookCommand testLookCommand;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    source = mock(Place.class);
    destination = mock(Place.class);
    when(destination.getName()).thenReturn("dest");
    when(destination.getId()).thenReturn(UUID.randomUUID());

    command = new TeleportCommand(actor, player, destination);

    testLookCommand = mock(LookCommand.class);
    try {
      when(testLookCommand.executeImpl()).thenReturn("here");
      command.setTestLookCommand(testLookCommand);
    } catch (CommandException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.getLocationAsPlace()).thenReturn(Optional.of(source));
    when(destination.passes(Category.GENERAL, player)).thenReturn(true);

    assertEquals("here", command.execute());

    verify(source).take(player);
    verify(destination).give(player);

    verify(player).setLocation(destination);
  }

  @Test
  public void testSuccessNoSource() throws Exception {
    when(player.getLocationAsPlace()).thenReturn(Optional.empty());
    when(destination.passes(Category.GENERAL, player)).thenReturn(true);

    assertEquals("here", command.execute());

    verify(destination).give(player);

    verify(player).setLocation(destination);
  }

  @Test
  public void testFailureNotPermitted() throws Exception {
    when(player.getLocationAsPlace()).thenReturn(Optional.of(source));
    when(destination.passes(Category.GENERAL, player)).thenReturn(false);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to move there", e.getMessage());
  }
}
