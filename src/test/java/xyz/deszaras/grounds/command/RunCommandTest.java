package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class RunCommandTest extends AbstractCommandTest {

  private RunCommand command;
  private CommandExecutor commandExecutor;
  private Optional<Actor> godActor;

  @BeforeEach
  public void setUp() {
    super.setUp();
    commandExecutor = mock(CommandExecutor.class, RETURNS_DEEP_STUBS);

    godActor = Player.GOD.getCurrentActor();
    Player.GOD.setCurrentActor(actor);
  }

  @AfterEach
  public void tearDown() {
    if (godActor.isPresent()) {
      Player.GOD.setCurrentActor(godActor.get());
    }
    Player.GOD.clearMessages();
  }

  @Test
  public void testSuccess() throws Exception {
    File commandFile = new File(ClassLoader.getSystemResource("runcommand1.cmd").getFile());
    command = new RunCommand(actor, Player.GOD, commandFile);
    command.setCommandExecutor(commandExecutor);

    Command[] commands = new Command[3];
    for (int i = 0; i < 3; i++) {
      commands[i] = mockCommand(List.of("NOOP" + i), Boolean.TRUE);
    }

    assertTrue(command.execute());

    for (int i = 0; i < 3; i++) {
      verify(commands[i]).execute();
      verifyMessages("NOOP" + i, "true");
    }
  }

  @Test
  public void testCommentsAndEmptyLines() throws Exception {
    File commandFile = new File(ClassLoader.getSystemResource("runcommand2.cmd").getFile());
    command = new RunCommand(actor, Player.GOD, commandFile);
    command.setCommandExecutor(commandExecutor);

    Command[] commands = new Command[2];
    for (int i = 0; i < 2; i++) {
      commands[i] = mockCommand(List.of("NOOP" + i), Boolean.TRUE);
    }

    assertTrue(command.execute());

    for (int i = 0; i < 2; i++) {
      verify(commands[i]).execute();
      verifyMessages("NOOP" + i, "true");
    }
  }

  @Test
  public void testFailureReadFile() throws Exception {
    command = new RunCommand(actor, Player.GOD, new File("nope.cmd"));

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("Failed to read"));
  }

  @Test
  public void testFailureCommandException() throws Exception {
    File commandFile = new File(ClassLoader.getSystemResource("runcommand1.cmd").getFile());
    command = new RunCommand(actor, Player.GOD, commandFile);
    command.setCommandExecutor(commandExecutor);

    Command[] commands = new Command[3];
    for (int i = 0; i < 3; i++) {
      if (i % 2 == 0) {
        commands[i] = mockCommand(List.of("NOOP" + i), Boolean.TRUE);
      } else {
        commands[i] = mock(Command.class);
        when(commandExecutor.getCommandFactory().getCommand(actor, Player.GOD, List.of("NOOP" + i)))
            .thenReturn(commands[i]);
        when(commands[i].execute()).thenThrow(new CommandException("oops"));
      }
    }

    assertFalse(command.execute());

    verify(commands[0]).execute();
    verifyMessages("NOOP0", "true");
    verify(commands[1]).execute();
    Message m = Player.GOD.getNextMessage();
    assertEquals("Running command:\nNOOP1", m.getMessage());
    m = Player.GOD.getNextMessage();
    assertTrue(m.getMessage().contains("oops"));
    verify(commands[2], never()).execute();
  }

  @Test
  public void testIgnoreExit() throws Exception {
    testIgnore("runcommand3.cmd", List.of("EXIT"), ExitCommand.class);
  }

  @Test
  public void testIgnoreSwitchPlayer() throws Exception {
    testIgnore("runcommand4.cmd", List.of("SWITCH_PLAYER", "Bob"),
               SwitchPlayerCommand.class);
  }

  private void testIgnore(String commandFileName,
                          List<String> ignoredCommandLine,
                          Class<? extends Command> ignoredCommandClass)
      throws Exception {
    File commandFile = new File(ClassLoader.getSystemResource(commandFileName).getFile());
    command = new RunCommand(actor, Player.GOD, commandFile);
    command.setCommandExecutor(commandExecutor);

    Command[] commands = new Command[3];
    commands[0] = mockCommand(List.of("NOOP0"), Boolean.TRUE);
    commands[1] = mockCommand(ignoredCommandLine, ignoredCommandClass, Boolean.TRUE);
    commands[2] = mockCommand(List.of("NOOP1"), Boolean.TRUE);

    assertTrue(command.execute());

    for (int i = 0; i < 3; i++) {
      verify(commands[i]).execute();
    }
    verifyMessages("NOOP0", "true");
    verifyMessages(String.join(" ", ignoredCommandLine), "true");
    Message m = Player.GOD.getNextMessage();
    assertTrue(m.getMessage().contains("Ignoring command of type"));
    verifyMessages("NOOP1", "true");
  }


  private Command mockCommand(List<String> commandLine, Object returnValue)
      throws Exception {
    return mockCommand(commandLine, Command.class, returnValue);
  }

  private Command mockCommand(List<String> commandLine, Class<? extends Command> commandClass,
                              Object returnValue)
      throws Exception {
    Command c = mock(commandClass);
    when(commandExecutor.getCommandFactory().getCommand(actor, Player.GOD, commandLine))
        .thenReturn(c);
    when(c.execute()).thenReturn(returnValue);
    return c;
  }

  private void verifyMessages(String command, String output)
      throws Exception {
    Message m = Player.GOD.getNextMessage();
    assertEquals("Running command:\n" + command, m.getMessage());
    m = Player.GOD.getNextMessage();
    assertEquals(output, m.getMessage());
  }
}
