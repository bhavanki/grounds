package xyz.deszaras.grounds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandExecutor.CommandResult;
import xyz.deszaras.grounds.command.CommandFactory;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.ShutdownCommand;
import xyz.deszaras.grounds.model.Player;

public class Shell implements Runnable {

  private final CommandExecutor commandExecutor;

  private BufferedReader in = null;
  private PrintWriter out = null;
  private PrintWriter err = null;
  private Actor actor = null;
  private Player player = null;
  private int exitCode = 0;
  private boolean exitedWithShutdown = false;

  public Shell(Actor actor) {
    this.actor = actor;

    CommandFactory commandFactory = new CommandFactory();
    commandExecutor = new CommandExecutor(commandFactory);
  }

  public void setIn(Reader in) {
    this.in = new BufferedReader(in);
  }
  public void setOut(Writer out) {
    this.out = new PrintWriter(out);
  }
  public void setErr(Writer err) {
    this.err = new PrintWriter(err);
  }

  public void setActor(Actor actor) {
    this.actor = actor;
  }
  public void setPlayer(Player player) {
    this.player = player;
  }

  public int getExitCode() {
    return exitCode;
  }

  public boolean exitedWithShutdown() {
    return exitedWithShutdown;
  }

  @Override
  public void run() {
    if (in == null || out == null || err == null) {
      throw new IllegalStateException("I/O is not connected!");
    }
    if (actor == null) {
      throw new IllegalStateException("The actor is not set!");
    }
    if (player == null) {
      throw new IllegalStateException("The current player is not set!");
    }

    try {
      while (true) {
        out.printf("# ");
        out.flush();
        String line = in.readLine();
        if (line == null) {
          break;
        }

        Future<CommandResult> commandFuture = commandExecutor.submit(actor, player, line);
        CommandResult commandResult;
        try {
          commandResult = commandFuture.get();

          if (!commandResult.isSuccessful()) {
            Optional<CommandException> commandException = commandResult.getCommandException();
            if (commandException.isPresent()) {
              err.printf("ERROR: %s\n", commandException.get().getMessage());
            }
          }
        } catch (ExecutionException e) {
          err.printf("ERROR: %s\n", e.getCause().getMessage());
          commandResult = new CommandResult(false, null);
        }

        if (commandResult.isSuccessful() &&
            commandResult.getCommandClass().isPresent() &&
            (commandResult.getCommandClass().get().equals(ExitCommand.class) ||
             commandResult.getCommandClass().get().equals(ShutdownCommand.class))) {
          if (commandResult.getCommandClass().get().equals(ShutdownCommand.class)) {
            exitedWithShutdown = true;
          }
          break;
        }

        String message;
        boolean wroteSeparator = false;
        while ((message = actor.getNextMessage()) != null) {
          if (!wroteSeparator) {
            out.printf("========================================\n");
            wroteSeparator = true;
          }
          out.printf("%s\n", message);
        }
        out.flush();

        out.printf(commandResult.isSuccessful() ? "√ " : "X ");
      }
    } catch (IOException e) {
      e.printStackTrace(err);
      out.println("I/O exception! Exiting");
      exitCode = 1;
    } catch (InterruptedException e) {
      e.printStackTrace(err);
      out.println("Interrupted! Exiting");
    } finally {
      commandExecutor.shutdown();
    }
  }

}
