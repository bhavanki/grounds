package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

public class CommandFactoryTest {

  private CommandFactory f;
  private Actor actor;
  private Player player;
  private Command c;

  @BeforeEach
  public void setUp() throws Exception {
    f = new CommandFactory(CommandExecutor.TRANSFORMS,
                           CommandExecutor.COMMANDS,
                           null);

    actor = mock(Actor.class);
    player = mock(Player.class);
  }

  @Test
  public void testNonServerNoArgs() throws Exception {
    c = f.getCommand(actor, player, List.of("INVENTORY"));

    assertTrue(c instanceof InventoryCommand);
    assertEquals(actor, c.getActor());
    assertEquals(player, c.getPlayer());
  }

  @Test
  public void testNonServerWithArgs() throws Exception {
    c = f.getCommand(actor, player, List.of("SAY", "Hello"));

    assertTrue(c instanceof SayCommand);
    assertEquals(actor, c.getActor());
    assertEquals(player, c.getPlayer());

    // TBD: check that command arguments were passed
  }

  @Test
  public void testServerNoArgs() throws Exception {
    Server server = mock(Server.class);
    f = new CommandFactory(CommandExecutor.TRANSFORMS,
                           CommandExecutor.COMMANDS,
                           server);

    c = f.getCommand(actor, player, List.of("WHO"));

    assertTrue(c instanceof WhoCommand);
    assertEquals(actor, c.getActor());
    assertEquals(player, c.getPlayer());
    assertEquals(server, ((WhoCommand) c).getServer());
  }

  @Test
  public void testTransform() throws Exception {
    c = f.getCommand(actor, player, List.of(">Hello"));

    assertTrue(c instanceof SayCommand);
    assertEquals(actor, c.getActor());
    assertEquals(player, c.getPlayer());

    // TBD: check that command arguments were passed
  }

  @Test
  public void testEmptyLine() throws Exception {
    c = f.getCommand(actor, player, List.of());

    assertTrue(c instanceof NoOpCommand);
    assertEquals(actor, c.getActor());
    assertEquals(player, c.getPlayer());
  }

  @Test
  public void testCFE() throws Exception {
    assertThrows(CommandFactoryException.class,
                 () -> f.getCommand(actor, player, List.of("DESCRIBE")));
  }
}
