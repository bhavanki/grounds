package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CommandCallableTest {

  private Command<Boolean> command;
  private CommandCallable callable;

  @BeforeEach
  public void setUp() {
    command = mock(Command.class);
  }

  @Test
  public void testCommandCallableWithFactory() throws Exception {
    Actor actor = Actor.ROOT;
    Player player = new Player("bob");
    List<String> commandLine = List.of();
    CommandFactory commandFactory = mock(CommandFactory.class);

    callable = new CommandCallable(actor, player, commandLine, commandFactory);

    when(commandFactory.getCommand(actor, player, commandLine))
        .thenReturn(command);
    when(command.execute()).thenReturn(true);

    CommandResult result = callable.call();

    assertTrue(result.isSuccessful());
    assertTrue((Boolean) result.getResult());
  }

  @Test
  public void testCommandCallableWithFactoryBuildFailure() throws Exception {
    Actor actor = Actor.ROOT;
    Player player = new Player("bob");
    List<String> commandLine = List.of();
    CommandFactory commandFactory = mock(CommandFactory.class);

    callable = new CommandCallable(actor, player, commandLine, commandFactory);

    CommandFactoryException e = new CommandFactoryException();
    when(commandFactory.getCommand(actor, player, commandLine)).thenThrow(e);
    when(command.execute()).thenReturn(true);

    CommandResult result = callable.call();

    assertFalse(result.isSuccessful());
    assertNull(result.getResult());
    assertEquals(e, result.getCommandFactoryException().get());
  }

  @Test
  public void testCommandCallableWithCommand() throws Exception {
    callable = new CommandCallable(command);

    when(command.execute()).thenReturn(true);

    CommandResult result = callable.call();

    assertTrue(result.isSuccessful());
    assertTrue((Boolean) result.getResult());
  }

  @Test
  public void testCommandCallableExecutionFailure() throws Exception {
    callable = new CommandCallable(command);

    CommandException e = new CommandException();
    when(command.execute()).thenThrow(e);

    CommandResult result = callable.call();

    assertFalse(result.isSuccessful());
    assertNull(result.getResult());
    assertEquals(e, result.getCommandException().get());
  }

}
