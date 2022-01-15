package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class CommandResultTest {

  private CommandResult<String> r;
  private Command command;

  @BeforeEach
  public void setUp() throws Exception {
    command = mock(Command.class);
  }

  @Test
  public void testSuccessfulResult() {
    r = new CommandResult<>("ok", command);

    assertTrue(r.isSuccessful());
    assertTrue(r.getCommandException().isEmpty());
    assertTrue(r.getCommandFactoryException().isEmpty());
    assertEquals("ok", r.getResult());
    assertEquals(command, r.getCommand().get());
  }

  @Test
  public void testFailureCEResult() {
    CommandException e = new CommandException("oops");
    r = new CommandResult<>(e);

    assertFalse(r.isSuccessful());
    assertEquals(e, r.getCommandException().get());
    assertTrue(r.getCommandFactoryException().isEmpty());
    assertNull(r.getResult());
    assertTrue(r.getCommand().isEmpty());
  }

  @Test
  public void testFailureCFEResult() {
    CommandFactoryException e = new CommandFactoryException("oops");
    r = new CommandResult<>(e);

    assertFalse(r.isSuccessful());
    assertTrue(r.getCommandException().isEmpty());
    assertEquals(e, r.getCommandFactoryException().get());
    assertNull(r.getResult());
    assertTrue(r.getCommand().isEmpty());
  }

  @Test
  public void testFailureMessageCE() {
    CommandException e = new CommandException("oopsie",
                                              new IllegalArgumentException("daisy"));
    r = new CommandResult<>(e);
    Player player = mock(Player.class);

    Message m = r.getFailureMessage(player);

    assertEquals(player, m.getSender());
    assertEquals(Message.Style.COMMAND_EXCEPTION, m.getStyle());
    assertEquals("ERROR: oopsie: daisy", m.getMessage());
  }

  @Test
  public void testFailureMessageCFE() {
    CommandFactoryException e = new CommandFactoryException("oopsie",
                                                            new IllegalArgumentException("daisy"));
    r = new CommandResult<>(e);
    Player player = mock(Player.class);

    Message m = r.getFailureMessage(player);

    assertEquals(player, m.getSender());
    assertEquals(Message.Style.COMMAND_FACTORY_EXCEPTION, m.getStyle());
    assertEquals("SYNTAX ERROR: oopsie: daisy", m.getMessage());
  }

  @Test
  public void testFailureMessageSuccessful() {
    r = new CommandResult<>("ok", command);
    Player player = mock(Player.class);

    assertThrows(IllegalStateException.class,
                 () -> r.getFailureMessage(player));
  }
}
