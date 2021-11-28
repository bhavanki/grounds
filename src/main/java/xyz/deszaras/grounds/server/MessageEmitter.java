package xyz.deszaras.grounds.server;

import java.util.Objects;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiString;

/**
 * A runnable that simply gets messages for a player and emits them
 * to their actor's terminal. This should be run in tandem with a {@link Shell}
 * that accepts and processes player inputs. The emitter uses a reference
 * to the shell's (or some other thing's) {@code LineReader} so that it
 * can emit messages above the input line.
 *
 * @see Shell
 */
public class MessageEmitter implements Runnable {

  private final Player player;
  private final Terminal terminal;
  private final LineReader lineReader;

  /**
   * Creates a new emitter.
   *
   * @param player target player for the emitter
   * @param terminal player's actor's terminal
   * @param lineReader line reader used to gather input
   * @throws NullPointerException if actor or terminal is null
   */
  public MessageEmitter(Player player, Terminal terminal, LineReader lineReader) {
    this.player = Objects.requireNonNull(player);
    this.terminal = Objects.requireNonNull(terminal);
    this.lineReader = Objects.requireNonNull(lineReader);
  }

  @Override
  public void run() {
    player.clearMessages();

    try {
      while (true) {
        Message nextMessage = player.getNextMessage();
        nextMessage = nextMessage.expandHorizontalRules(terminal.getWidth());
        LineBreaker lineBreaker = new LineBreaker(terminal.getWidth());
        AnsiString styledMessage = nextMessage.getStyledMessage();
        AnsiString emitMessage = lineBreaker.insertLineBreaks(styledMessage);
        lineReader.printAbove(emitMessage.toString());
      }
    } catch (InterruptedException e) { // NOPMD
      // exit the thread
    }
  }
}
