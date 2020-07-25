package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkArrivalEvent;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkDepartureEvent;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class YoinkCommandTest extends AbstractCommandTest {

  private Player yoinkedPlayer;
  private Place source;
  private Place destination;
  private YoinkCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.THAUMATURGE);

    yoinkedPlayer = mock(Player.class);
    source = mock(Place.class);
    destination = mock(Place.class);

    command = new YoinkCommand(actor, player, yoinkedPlayer, destination);
  }

  @Test
  public void testSuccess() throws Exception {
    when(yoinkedPlayer.getLocation()).thenReturn(Optional.of(source));

    assertTrue(command.execute());

    verify(source).take(yoinkedPlayer);
    verify(destination).give(yoinkedPlayer);

    verify(yoinkedPlayer).setLocation(destination);

    verifyEvent(new YoinkDepartureEvent(player, source), command);
    verifyEvent(new YoinkArrivalEvent(player, destination), command);
  }

  @Test
  public void testSuccessNoSource() throws Exception {
    when(yoinkedPlayer.getLocation()).thenReturn(Optional.empty());

    assertTrue(command.execute());

    verify(destination).give(yoinkedPlayer);

    verify(yoinkedPlayer).setLocation(destination);

    verifyEvent(new YoinkArrivalEvent(player, destination), command);
  }
}
