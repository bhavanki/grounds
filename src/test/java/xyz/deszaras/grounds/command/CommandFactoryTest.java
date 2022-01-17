package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.Server;

public class CommandFactoryTest {

  private static final List<BiFunction<List<String>, Player, List<String>>> TEST_TRANSFORMS;
  private static final Map<String, Class<? extends Command>> TEST_COMMANDS;

  // Subsets of CommandExecutor
  static {
    TEST_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("DESCRIBE", DescribeCommand.class)
        .put("SAY", SayCommand.class)
        .put("INVENTORY", InventoryCommand.class)
        .put("WHO", WhoCommand.class)
        .put("MALFORMED", MalformedCommand.class)
        .build();

    TEST_TRANSFORMS = ImmutableList.<BiFunction<List<String>, Player, List<String>>>builder()
        .add((line, player) -> {
            if (line.get(0).startsWith(">")) {
              return ImmutableList.<String>builder()
                  .add("SAY")
                  .add(line.get(0).substring(1))
                  .addAll(line.subList(1, line.size()))
                  .build();
            }
            return line;
          })
        .build();
  }

  private CommandFactory f;
  private Actor actor;
  private Player player;
  private Command c;

  @BeforeEach
  public void setUp() throws Exception {
    f = new CommandFactory(TEST_TRANSFORMS, TEST_COMMANDS, null);

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
    f = new CommandFactory(TEST_TRANSFORMS, TEST_COMMANDS, server);

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
  public void testUnrecognizedCommand() throws Exception {
    CommandFactoryException e =
        assertThrows(CommandFactoryException.class,
                     () -> f.getCommand(actor, player, List.of("FOO")));
    assertTrue(e.getMessage().contains("Unrecognized command"));
  }

  // missing newCommand
  static class MalformedCommand extends Command<Boolean> {
    public MalformedCommand(Actor actor, Player player) {
      super(actor, player);
    }

    @Override
    protected Boolean executeImpl() {
      return true;
    }
  }

  @Test
  public void testMissingNewCommand() throws Exception {
    CommandFactoryException e =
        assertThrows(CommandFactoryException.class,
                     () -> f.getCommand(actor, player, List.of("MALFORMED")));
    assertTrue(e.getMessage().contains("lacks a static newCommand method"));
  }

  @Test
  public void testCFE() throws Exception {
    assertThrows(CommandFactoryException.class,
                 () -> f.getCommand(actor, player, List.of("DESCRIBE")));
  }
}
