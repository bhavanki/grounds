package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.Actor;

public class ShellTest {

  private Actor actor;
  private Terminal terminal;
  private ExecutorService ees;
  private LineReader lineReader;
  private Shell shell;

  @BeforeEach
  public void setUp() {
    actor = new Actor("actor1");
    terminal = mock(Terminal.class);
    ees = Executors.newSingleThreadExecutor();
    lineReader = mock(LineReader.class);

    shell = new Shell(actor, terminal, ees, lineReader);
  }

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String TEST_IP = "203.0.113.123";

  @Test
  public void testGetters() throws Exception {
    assertEquals(actor, shell.getActor());

    actor.setMostRecentIPAddress(InetAddress.getByName(TEST_IP));
    assertEquals(TEST_IP, shell.getIPAddress());

    assertEquals(lineReader, shell.getLineReader());

    Instant now = Instant.now();
    shell.setStartTime(now);
    assertEquals(now, shell.getStartTime());

    assertTrue(shell.getPlayer().isEmpty());

    assertEquals(0, shell.getExitCode());
  }

  @Test
  public void testTerminate() {
    assertFalse(shell.terminate());

    Future<Integer> shellFuture = new CompletableFuture<>();
    shell.setFuture(shellFuture);

    assertTrue(shell.terminate());
    assertTrue(shellFuture.isCancelled());
  }
}
