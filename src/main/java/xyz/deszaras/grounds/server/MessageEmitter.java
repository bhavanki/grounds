package xyz.deszaras.grounds.server;

import java.io.PrintWriter;
import org.jline.terminal.Terminal;
import xyz.deszaras.grounds.command.Actor;

/**
 * A runnable that simply gets messages for an actor and emits them
 * to their terminal. This should be run in tandem with a {@link Shell}
 * that accepts and processes actor inputs.
 *
 * @see Shell
 */
public class MessageEmitter implements Runnable {

  private final Actor actor;
  private final Terminal terminal;

  /**
   * Creates a new emitter.
   *
   * @param actor target actor for the emitter
   * @param terminal actor's terminal
   */
  public MessageEmitter(Actor actor, Terminal terminal) {
    this.actor = actor;
    this.terminal = terminal;
  }

  @Override
  public void run() {
    PrintWriter out = terminal.writer();

    try {
      while (true) {
        String message = actor.getNextMessage();
        out.printf("%s\n", message);
      }
    } catch (InterruptedException e) { // NOPMD
      // exit the thread
    }
  }
}
