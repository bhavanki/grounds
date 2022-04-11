package xyz.deszaras.grounds.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiString;

public class MessageEmitterTest {

  private static ExecutorService emitterExecutorService;

  private Actor actor;
  private Player player;
  private Terminal terminal;
  private LineReader lineReader;

  private MessageEmitter emitter;

  private Player sender;

  @BeforeAll
  public static void setUpClass() {
    emitterExecutorService = Executors.newSingleThreadExecutor();
  }

  @AfterAll
  public static void tearDownClass() {
    emitterExecutorService.shutdown();
  }

  @BeforeEach
  public void setUp() {
    actor = mock(Actor.class);
    player = mock(Player.class);
    terminal = mock(Terminal.class);
    lineReader = mock(LineReader.class);

    emitter = new MessageEmitter(player, terminal, lineReader);
    when(player.getCurrentActor()).thenReturn(Optional.of(actor));

    sender = new Player("jay");
  }

  @Test
  public void testRun() throws Exception {
    when(actor.getPreference(Actor.PREFERENCE_ANSI))
        .thenReturn(Optional.of("true"));
    when(terminal.getWidth()).thenReturn(80);
    when(player.getNextMessage())
        .thenReturn(new Message(sender, Message.Style.INFO, "one"))
        .thenReturn(new Message(sender, Message.Style.INFO, "two"))
        .thenReturn(new Message(sender, Message.Style.INFO, "three"))
        .thenThrow(new InterruptedException());

    Future<?> future = emitterExecutorService.submit(emitter);
    future.get();

    verify(player).clearMessages();
    verify(lineReader).printAbove("one");
    verify(lineReader).printAbove("two");
    verify(lineReader).printAbove("three");
  }

  @Test
  public void testLineBreaking() throws Exception {
    when(actor.getPreference(Actor.PREFERENCE_ANSI))
        .thenReturn(Optional.of("true"));
    when(terminal.getWidth()).thenReturn(20);
    when(player.getNextMessage())
        .thenReturn(new Message(sender, Message.Style.INFO,
                                "please break this message into three lines"))
        .thenThrow(new InterruptedException());

    Future<?> future = emitterExecutorService.submit(emitter);
    future.get();

    verify(player).clearMessages();
    verify(lineReader).printAbove("please break this\nmessage into three\nlines");
  }

  @Test
  public void testExpandHorizontalRules() throws Exception {
    when(actor.getPreference(Actor.PREFERENCE_ANSI))
        .thenReturn(Optional.of("true"));
    when(terminal.getWidth()).thenReturn(20);
    when(player.getNextMessage())
        .thenReturn(new Message(sender, Message.Style.INFO, "one\n{hr =}\ntwo"))
        .thenThrow(new InterruptedException());

    Future<?> future = emitterExecutorService.submit(emitter);
    future.get();

    verify(player).clearMessages();
    verify(lineReader).printAbove("one\n====================\ntwo");
  }

  @Test
  public void testStyles() throws Exception {
    when(actor.getPreference(Actor.PREFERENCE_ANSI))
        .thenReturn(Optional.of("true"));
    when(terminal.getWidth()).thenReturn(80);
    when(player.getNextMessage())
        .thenReturn(new Message(sender, Message.Style.POSE, "one"))
        .thenReturn(new Message(sender, Message.Style.OOC, "two"))
        .thenReturn(new Message(sender, Message.Style.SAY, "three"))
        .thenThrow(new InterruptedException());

    Future<?> future = emitterExecutorService.submit(emitter);
    future.get();

    LineBreaker lb = new LineBreaker(80);
    AnsiString expectedOne = lb.insertLineBreaks(new AnsiString(Message.Style.POSE.format("one")));
    AnsiString expectedTwo = lb.insertLineBreaks(new AnsiString(Message.Style.OOC.format("two")));
    AnsiString expectedThree = lb.insertLineBreaks(new AnsiString(Message.Style.SAY.format("three")));

    verify(player).clearMessages();
    verify(lineReader).printAbove(expectedOne.toString());
    verify(lineReader).printAbove(expectedTwo.toString());
    verify(lineReader).printAbove(expectedThree.toString());
  }
}
