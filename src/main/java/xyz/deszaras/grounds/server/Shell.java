package xyz.deszaras.grounds.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandExecutor.CommandResult;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.ShutdownCommand;
import xyz.deszaras.grounds.command.SwitchPlayerCommand;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;

/**
 * A shell interface for an actor. When run, a shell receives and
 * process commands until the actor exits. While it does provide some
 * output to the actor's terminal, most of it should go through an
 * accompanying {@link MessageEmitter}, so that messages are delivered
 * even while the shell is waiting for input.
 *
 * @see CommandExecutor
 * @see MessageEmitter
 */
public class Shell implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Shell.class);

  private final Actor actor;
  private final Terminal terminal;
  private final LineReader lineReader;

  private Player player = null;
  private String bannerContent = null;
  private int exitCode = 0;
  private boolean exitedWithShutdown = false;

  /**
   * Creates a new shell.
   *
   * @param actor actor using the shell
   * @param terminal actor's terminal
   */
  public Shell(Actor actor, Terminal terminal) {
    this.actor = actor;
    this.terminal = terminal;
    lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .build();
  }

  /**
   * Gets the line reader used by the shell.
   *
   * @return line reader
   */
  public LineReader getLineReader() {
    return lineReader;
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
   * Sets the banner content for the shell.
   *
   * @param bannerContent banner content
   */
  public void setBannerContent(String bannerContent) {
    this.bannerContent = bannerContent;
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

  // https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
  // This doesn't obey escaped quotes, though.
  private static final Pattern TOKENIZE_PATTERN =
      Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  /**
   * Splits a line of text into tokens. Generally, tokens are separated
   * by whitespace, but text surrounded by single or double quotes
   * is kept together as a single token (without the quotes).
   *
   * @param line line of text
   * @return tokens in line
   */
  @VisibleForTesting
  static List<String> tokenize(String line) {
    List<String> tokens = new ArrayList<>();
    Matcher m = TOKENIZE_PATTERN.matcher(line);
    while (m.find()) {
      if (m.group(1) != null) {
        // quotation marks
        tokens.add(m.group(1));
      } else if (m.group(2) != null) {
        // apostrophes
        tokens.add(m.group(2));
      } else {
        tokens.add(m.group());
      }
    }
    return tokens;
  }

  @Override
  public void run() {
    PrintWriter out = terminal.writer();
    PrintWriter err = terminal.writer(); // sadly

    try {
      emitBanner();

      if (player == null) {
        try {
          player = selectPlayer();
          player.setCurrentActor(actor);
        } catch (UserInterruptException | EndOfFileException e) {
          return;
        }
        if (player == null) {
          return;
        }
      }

      String prePrompt = "";
      String prompt = getPrompt(player);

      while (true) {
        String line;
        try {
          line = lineReader.readLine(prePrompt + prompt);
        } catch (UserInterruptException | EndOfFileException e) {
          break;
        }
        List<String> tokens = tokenize(line);
        prePrompt = "√ ";
        if (!tokens.isEmpty()) {

          Future<CommandResult> commandFuture =
              CommandExecutor.INSTANCE.submit(actor, player, tokens);
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
            } else if (commandClass.equals(ActorCommand.class)) {
              try {
                ActorDatabase.INSTANCE.save();
              } catch (IOException e) {
                LOG.error("Failed to save actor database", e);
                actor.sendMessage("Failed to save actor database, check the logs");
              }
            }
          }

          if (!commandResult.isSuccessful()) {
            prePrompt = "X ";
          }
        }
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

  private void emitBanner() {
    if (bannerContent == null) {
      return;
    }
    String bannerToEmit = bannerContent
        .replaceAll("_username_", actor.getUsername());
    terminal.writer().println(bannerToEmit);
  }

  private Player selectPlayer() throws IOException {
    List<Player> permittedPlayers =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername())
        .get().getPlayers().stream()
        .map(id -> Multiverse.MULTIVERSE.findThing(id, Player.class))
        .filter(p -> p.isPresent())
        .map(p -> p.get())
        .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
        .collect(Collectors.toList());
    terminal.writer().println("Permitted players:");
    for (Player p : permittedPlayers) {
      terminal.writer().printf("  %s\n", p.getName());
    }
    terminal.writer().println("");
    selection:
    while (true) {
      terminal.writer().printf("Select your initial player: ");
      String line = lineReader.readLine();
      if (line == null) {
        return null;
      }
      for (Player p : permittedPlayers) {
        if (!p.getName().equals(line)) {
          continue;
        }
        if (p.getCurrentActor().isPresent()) {
          terminal.writer().printf("Someone is already playing as %s\n\n",
                                   p.getName());
          continue selection;
        }
        return p;
      }
      terminal.writer().printf("That is not a permitted player\n\n");
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
