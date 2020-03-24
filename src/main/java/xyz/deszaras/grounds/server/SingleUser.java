package xyz.deszaras.grounds.server;

import java.io.Console;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;

/**
 * A single-user session, which doesn't involve starting the server.
 * The actor automatically plays as GOD.
 */
public class SingleUser implements Runnable {

  private final Properties serverProperties;

  public SingleUser(Properties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Override
  public void run() {
    Console console = System.console();
    if (console == null) {
      throw new IllegalStateException("No console!");
    }

    String actorDatabaseFileProperty = serverProperties.getProperty("actorDatabaseFile");
    if (actorDatabaseFileProperty == null) {
      throw new IllegalStateException("No actorDatabaseFile specified");
    }
    Path actorDatabaseFile = FileSystems.getDefault().getPath(actorDatabaseFileProperty);
    ActorDatabase.INSTANCE.setPath(actorDatabaseFile);
    if (actorDatabaseFile.toFile().exists()) {
      try {
        ActorDatabase.INSTANCE.load();
      } catch (IOException e) {
        console.printf("Failed to load actor database from " + actorDatabaseFile);
        return;
      }
    } else {
      console.printf("Actor database does not yet exist, not loading\n\n");
    }

    console.printf("Welcome to Grounds.\n");
    console.printf("This is single-user mode. Use ^D or 'exit' to quit.\n\n");

    Actor actor = Actor.ROOT;
    Terminal localTerminal;
    try {
      localTerminal = TerminalBuilder.terminal();
    } catch (IOException e) {
      console.printf("Failed to allocate local terminal", e);
      return;
    }

    Thread emitterThread = new Thread(new MessageEmitter(actor, localTerminal));
    emitterThread.start();

    Shell shell = new Shell(actor, localTerminal);
    shell.setPlayer(Player.GOD);

    try {
      shell.run();
    } finally {
      emitterThread.interrupt();
      try {
        localTerminal.close();
      } catch (IOException e) {
        console.printf("Failed to close local terminal", e);
      }
    }
    CommandExecutor.INSTANCE.shutdown();

    console.printf("\n\nBye!\n");
  }

}
