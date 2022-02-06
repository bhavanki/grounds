package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.BuildCommand;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.DestroyCommand;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ConciergeTest {

  private Universe universe;
  private CommandExecutor commandExecutor;
  private Concierge concierge;

  private Place guestHome;

  @BeforeEach
  public void setUp() {
    universe = mock(Universe.class);
    commandExecutor = mock(CommandExecutor.class);
    concierge = new Concierge(universe, commandExecutor);

    concierge.resetGuestCounter();

    guestHome = mock(Place.class);
    when(universe.getGuestHomePlace()).thenReturn(guestHome);
  }

  @Test
  public void testBuildGuestPlayer() {
    Future<CommandResult<String>> buildResultFuture =
        Futures.immediateFuture(new CommandResult("id1", null));
    doReturn(buildResultFuture).when(commandExecutor).submit(any(BuildCommand.class));
    Player guest1 = mock(Player.class);
    when(universe.getThing("id1", Player.class)).thenReturn(Optional.of(guest1));

    Player builtPlayer = concierge.buildGuestPlayer();

    verify(builtPlayer).setCurrentActor(Actor.GUEST);
    verify(builtPlayer).setHome(guestHome);

    ArgumentCaptor<BuildCommand> buildCommandCaptor = ArgumentCaptor.forClass(BuildCommand.class);
    verify(commandExecutor).submit(buildCommandCaptor.capture());
    BuildCommand buildCommand = buildCommandCaptor.getValue();
    assertEquals(Actor.ROOT, buildCommand.getActor());
    assertEquals(Player.GOD, buildCommand.getPlayer());
    assertEquals("player", buildCommand.getType());
    assertEquals("guest1", buildCommand.getName());
    assertEquals(List.of("guest"), buildCommand.getBuildArgs());
  }

  @Test
  public void testDestroyGuestPlayer() {
    Future<CommandResult<String>> destroyResultFuture =
        Futures.immediateFuture(new CommandResult(Boolean.TRUE, null));
    doReturn(destroyResultFuture).when(commandExecutor).submit(any(DestroyCommand.class));
    Player guest1 = mock(Player.class);
    when(guest1.trySetCurrentActor(null, Actor.GUEST)).thenReturn(true);

    concierge.destroyGuestPlayer(guest1);

    ArgumentCaptor<DestroyCommand> destroyCommandCaptor = ArgumentCaptor.forClass(DestroyCommand.class);
    verify(commandExecutor).submit(destroyCommandCaptor.capture());
    DestroyCommand destroyCommand = destroyCommandCaptor.getValue();
    assertEquals(Actor.ROOT, destroyCommand.getActor());
    assertEquals(Player.GOD, destroyCommand.getPlayer());
    assertEquals(guest1, destroyCommand.getThing());
  }

  @Test
  public void testDestroyGuestPlayerNotAGuest() {
    Player guest1 = mock(Player.class);
    when(guest1.trySetCurrentActor(null, Actor.GUEST)).thenReturn(false);

    assertThrows(IllegalArgumentException.class,
                 () -> concierge.destroyGuestPlayer(guest1));

    verify(commandExecutor, never()).submit(any(DestroyCommand.class));
  }
}
