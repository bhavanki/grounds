package xyz.deszaras.grounds.server;

import java.io.Console;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;

/**
 * A single-user session, which doesn't involve starting the server.
 * The actor automatically plays as GOD.
 */
public class SingleUser implements Runnable {

  @Override
  public void run() {
    Console console = System.console();
    if (console == null) {
      throw new IllegalStateException("No console!");
    }
    console.printf("Welcome to Grounds.\n");
    console.printf("This is single-user mode. Use ^D or 'exit' to quit.\n\n");

    Actor actor = new Actor("root");
    Shell shell = new Shell(actor);
    shell.setIn(console.reader());
    shell.setOut(console.writer());
    shell.setErr(console.writer());
    shell.setPlayer(Player.GOD);

    shell.run();
    CommandExecutor.INSTANCE.shutdown();

    console.printf("\n\nBye!\n");
  }

}
