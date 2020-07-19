package xyz.deszaras.grounds.server;

import java.io.Console;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.Argon2Utils;

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
      console.printf("Actor database does not yet exist, creating a new one\n\n");
      ActorDatabase.INSTANCE.createActorRecord(Actor.ROOT.getUsername(),
                                               Argon2Utils.hashPassword("grounds"));
      ActorDatabase.INSTANCE.createActorRecord(Actor.GUEST.getUsername(),
                                               Argon2Utils.hashPassword("guest"));
      try {
        ActorDatabase.INSTANCE.save();
      } catch (IOException e) {
        console.printf("Failed to save new actor database", e);
        return;
      }
      console.printf("Created new actor database. Please set the root password:\n\n" +
                     "ACTOR SET_PASSWORD root <new-password>\n\n" +
                     "Also created new guest actor with password 'guest'.\n");
    }

    CommandExecutor.create(null);

    console.printf("Welcome to Grounds.\n");
    console.printf("This is single-user mode. Use ^D or 'exit' to quit.\n\n");

    Actor actor = Actor.ROOT;
    ActorDatabase.ActorRecord actorRecord =
      ActorDatabase.INSTANCE.getActorRecord(actor.getUsername()).get();
    actor.setPreferences(actorRecord.getPreferences());

    Terminal localTerminal;
    try {
      localTerminal = TerminalBuilder.terminal();
    } catch (IOException e) {
      console.printf("Failed to allocate local terminal", e);
      return;
    }

    ExecutorService emitterExecutorService = Executors.newCachedThreadPool();
    Shell shell = new Shell(actor, localTerminal, emitterExecutorService);
    shell.setPlayer(Player.GOD);
    Universe.getCurrent().addThing(Player.GOD);
    Universe.getCurrent().getOriginPlace().give(Player.GOD);
    Player.GOD.setLocation(Universe.getCurrent().getOriginPlace());
    Player.GOD.setCurrentActor(Actor.ROOT);

    try {
      shell.run();
    } finally {
      emitterExecutorService.shutdown();
      try {
        localTerminal.close();
      } catch (IOException e) {
        console.printf("Failed to close local terminal", e);
      }
    }
    CommandExecutor.getInstance().shutdown();

    console.printf("\n\nBye!\n");
  }

}
