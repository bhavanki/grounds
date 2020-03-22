package xyz.deszaras.grounds.server;

import com.google.common.base.Throwables;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandExecutor.CommandResult;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.ShutdownCommand;
import xyz.deszaras.grounds.command.SwitchPlayerCommand;
import xyz.deszaras.grounds.model.Player;

/**
 * A shell interface for an actor. When run, a shell receives and
 * process commands until the actor exits.
 *
 * @see CommandExecutor
 */
public class Shell implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Shell.class);

  private final Actor actor;

  private BufferedReader in = null;
  private PrintWriter out = null;
  private PrintWriter err = null;
  private Player player = null;
  private int exitCode = 0;
  private boolean exitedWithShutdown = false;

  /**
   * Creates a new shell.
   *
   * @param actor actor using the shell
   */
  public Shell(Actor actor) {
    this.actor = actor;
  }

  /**
   * Sets the input reader for the shell.
   *
   * @param in input reader
   */
  public void setIn(Reader in) {
    this.in = new BufferedReader(in);
  }
  /**
   * Sets the output writer for the shell.
   *
   * @param out output writer
   */
  public void setOut(Writer out) {
    this.out = new PrintWriter(out);
  }
  /**
   * Sets the error writer for the shell.
   *
   * @param err error writer
   */
  public void setErr(Writer err) {
    this.err = new PrintWriter(err);
  }

  /**
   * Sets the player for the shell.
   *
   * @param player player
   */
  public void setPlayer(Player player) {
    this.player = player;
  }

  /**
   * Gets the exit code for the shell.
   *
   * @return exit code
   */
  public int getExitCode() {
    return exitCode;
  }

  /**
   * Checks if the shell exited due to a shutdown request.
   *
   * @return true is shutdown was requested
   */
  public boolean exitedWithShutdown() {
    return exitedWithShutdown;
  }

  @Override
  public void run() {
    if (in == null || out == null || err == null) {
      throw new IllegalStateException("I/O is not connected!");
    }
    if (player == null) {
      throw new IllegalStateException("The current player is not set!");
    }

    String prompt = getPrompt(player);

    try {
      while (true) {
        out.printf(prompt);
        out.flush();
        String line = in.readLine();
        if (line == null) {
          break;
        }

        Future<CommandResult> commandFuture =
            CommandExecutor.INSTANCE.submit(actor, player, line);
        CommandResult commandResult;
        try {
          commandResult = commandFuture.get();

          if (!commandResult.isSuccessful()) {
            Optional<CommandFactoryException> commandFactoryException =
                commandResult.getCommandFactoryException();
            if (commandFactoryException.isPresent()) {
              err.printf("SYNTAX ERROR: %s\n", joinMessages(commandFactoryException.get()));
              LOG.info("Command build failed for actor {}", actor.getUsername(),
                       commandFactoryException);
            }
          }
        } catch (ExecutionException e) {
          err.printf("ERROR: %s\n", joinMessages(e.getCause()));
          LOG.info("Command execution failed for actor {}", actor.getUsername(),
                   e.getCause());
          commandResult = new CommandResult(false, null);
        }
        err.flush();

        if (commandResult.isSuccessful() &&
            commandResult.getCommandClass().isPresent()) {
          Class<? extends Command> commandClass = commandResult.getCommandClass().get();
          if (commandClass.equals(ExitCommand.class) ||
              commandClass.equals(ShutdownCommand.class)) {
            if (commandClass.equals(ShutdownCommand.class)) {
              exitedWithShutdown = true;
            }
            break;
          } else if (commandClass.equals(SwitchPlayerCommand.class)) {
            player = actor.getCurrentPlayer();
            prompt = getPrompt(player);
          }
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
    }
  }

  private static String getPrompt(Player player) {
    if (player.equals(Player.GOD)) {
      return "# ";
    } else {
      return "$ ";
    }
  }

  private static String joinMessages(Throwable e) {
    return Throwables.getCausalChain(e).stream()
        .map(t -> t.getMessage())
        .collect(Collectors.joining(": "));
  }
}
