package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CommandCallableTest {

  private Command<Boolean> command;
  private CommandExecutor commandExecutor;
  private EventBus commandEventBus;
  private CommandCallable callable;

  private static class TestEvent extends Event<String> {
    private TestEvent(Player player, Place place) {
      super(player, place, "test");
    }
  }

  @BeforeEach
  public void setUp() {
    command = mock(Command.class);
    commandExecutor = mock(CommandExecutor.class);
    commandEventBus = mock(EventBus.class);
    when(commandExecutor.getCommandEventBus()).thenReturn(commandEventBus);
  }

  @Test
  public void testCommandCallableWithFactory() throws Exception {
    Actor actor = Actor.ROOT;
    Player player = new Player("bob");
    List<String> commandLine = List.of();
    CommandFactory commandFactory = mock(CommandFactory.class);
    when(commandExecutor.getCommandFactory()).thenReturn(commandFactory);

    callable = new CommandCallable(actor, player, commandLine, commandExecutor);

    when(commandFactory.getCommand(actor, player, commandLine))
        .thenReturn(command);
    when(command.execute()).thenReturn(true);
    Place place = new Place("there");
    when(command.getEvents()).thenReturn(Set.of(new TestEvent(player, place)));

    CommandResult result = callable.call();

    assertTrue(result.isSuccessful());
    assertTrue((Boolean) result.getResult());
    verify(commandEventBus).post(any(TestEvent.class));
  }

  @Test
  public void testCommandCallableWithFactoryBuildFailure() throws Exception {
    Actor actor = Actor.ROOT;
    Player player = new Player("bob");
    List<String> commandLine = List.of();
    CommandFactory commandFactory = mock(CommandFactory.class);
    when(commandExecutor.getCommandFactory()).thenReturn(commandFactory);

    callable = new CommandCallable(actor, player, commandLine, commandExecutor);

    CommandFactoryException e = new CommandFactoryException();
    when(commandFactory.getCommand(actor, player, commandLine)).thenThrow(e);
    when(command.execute()).thenReturn(true);
    Place place = new Place("there");
    when(command.getEvents()).thenReturn(Set.of(new TestEvent(player, place)));

    CommandResult result = callable.call();

    assertFalse(result.isSuccessful());
    assertNull(result.getResult());
    assertEquals(e, result.getCommandFactoryException().get());
    verify(commandEventBus, never()).post(any(TestEvent.class));
  }

  @Test
  public void testCommandCallableWithCommand() throws Exception {
    callable = new CommandCallable(command, commandExecutor);

    when(command.execute()).thenReturn(true);
    Place place = new Place("there");
    when(command.getEvents()).thenReturn(Set.of(new TestEvent(Player.GOD, place)));

    CommandResult result = callable.call();

    assertTrue(result.isSuccessful());
    assertTrue((Boolean) result.getResult());
    verify(commandEventBus).post(any(TestEvent.class));
  }

  @Test
  public void testCommandCallableExecutionFailure() throws Exception {
    callable = new CommandCallable(command, commandExecutor);

    CommandException e = new CommandException();
    when(command.execute()).thenThrow(e);

    CommandResult result = callable.call();

    assertFalse(result.isSuccessful());
    assertNull(result.getResult());
    assertEquals(e, result.getCommandException().get());
    verify(commandEventBus, never()).post(any(TestEvent.class));
  }

}
