package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class CommandExecutorTest {

  private CommandFactory factory;
  private EventBus eventBus;
  private CommandExecutor executor;

  @BeforeEach
  public void setUp() {
    factory = mock(CommandFactory.class);
    eventBus = mock(EventBus.class);
    executor = new CommandExecutor(factory, eventBus);
  }

  @AfterEach
  public void tearDown() {
    executor.shutdown();
  }

  @Test
  public void testGetters() {
    assertEquals(factory, executor.getCommandFactory());
    assertEquals(eventBus, executor.getCommandEventBus());
  }

  @Test
  public void testSubmitCommandLine() throws Exception {
    Command<Integer> command = mock(Command.class);
    when(command.execute()).thenReturn(42);

    Actor actor = mock(Actor.class);
    Player player = mock(Player.class);
    List<String> commandLine = List.of("go");
    // generics, gotta love 'em
    doReturn(command).when(factory).getCommand(actor, player, commandLine);

    Future<CommandResult<Integer>> future = executor.submit(command);

    assertEquals(42, future.get().getResult());
  }

  @Test
  public void testSubmitCommand() throws Exception {
    Command<Integer> command = mock(Command.class);
    when(command.execute()).thenReturn(42);

    Future<CommandResult<Integer>> future = executor.submit(command);

    assertEquals(42, future.get().getResult());
  }
}
