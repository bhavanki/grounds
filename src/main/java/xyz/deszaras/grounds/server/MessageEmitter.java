package xyz.deszaras.grounds.server;

import java.io.PrintWriter;
import java.util.Objects;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import xyz.deszaras.grounds.command.Actor;

/**
 * A runnable that simply gets messages for an actor and emits them
 * to their terminal. This should be run in tandem with a {@link Shell}
 * that accepts and processes actor inputs. The emitter uses a reference
 * to the shell's (or some other thing's) {@code LineReader} so that it
 * can emit message above the input line.
 *
 * @see Shell
 */
public class MessageEmitter implements Runnable {

  private final Actor actor;
  private final Terminal terminal;
  private final LineReader lineReader;

  /**
   * Creates a new emitter.
   *
   * @param actor target actor for the emitter
   * @param terminal actor's terminal
   * @param lineReader line reader used to gather input (may be null)
   * @throws NullPointerException if actor or terminal is null
   */
  public MessageEmitter(Actor actor, Terminal terminal, LineReader lineReader) {
    this.actor = Objects.requireNonNull(actor);
    this.terminal = Objects.requireNonNull(terminal);
    this.lineReader = Objects.requireNonNull(lineReader);
  }

  @Override
  public void run() {
    PrintWriter out = terminal.writer();

    try {
      while (true) {
        String sentMessage = actor.getNextMessage().getFormattedMessage();
        LineBreaker lineBreaker = new LineBreaker(terminal.getWidth());
        String message = lineBreaker.insertLineBreaks(sentMessage);
        if (lineReader != null) {
          lineReader.printAbove(message);
        } else {
          out.printf("%s\n", message);
        }
      }
    } catch (InterruptedException e) { // NOPMD
      // exit the thread
    }
  }
}
