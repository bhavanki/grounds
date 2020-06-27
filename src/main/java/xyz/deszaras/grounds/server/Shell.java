package xyz.deszaras.grounds.server;

import com.google.common.base.Throwables;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.fusesource.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.SwitchPlayerCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;
import xyz.deszaras.grounds.util.CommandLineUtils;

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
  private final ExecutorService emitterExecutorService;
  private final LineReader lineReader;

  private Player player = null;
  private String bannerContent = null;
  private Future<?> future = null;
  private int exitCode = 0;

  /**
   * Creates a new shell.
   *
   * @param actor actor using the shell
   * @param terminal actor's terminal
   */
  public Shell(Actor actor, Terminal terminal, ExecutorService emitterExecutorService) {
    this.actor = actor;
    this.terminal = terminal;
    this.emitterExecutorService = emitterExecutorService;
    lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .build();
  }

  /**
   * Gets the IP address for the actor using this shell.
   *
   * @return IP address
   */
  public String getIPAddress() {
    return InetAddresses.toAddrString(actor.getMostRecentIPAddress());
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
   * Gets the player for the shell.
   *
   * @return player
   */
  public Player getPlayer() {
    return player;
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
   * Sets the future for the task that executes this shell.
   *
   * @param future future for this shell
   */
  public void setFuture(Future<?> future) {
    this.future = future;
  }

  /**
   * Gets the exit code for the shell.
   *
   * @return exit code
   */
  public int getExitCode() {
    return exitCode;
  }

  @Override
  public void run() {
    PrintWriter out = terminal.writer();
    PrintWriter err = terminal.writer(); // sadly

    String ansiPref = actor.getPreference(Actor.PREFERENCE_ANSI).orElse("false");
    if (!("true".equals(ansiPref))) {
      // The flag is an InheritedThreadLocal, so it only affects this shell.
      Ansi.setEnabled(false);
    }

    Future<?> emitterFuture = null;

    try {
      emitBanner();

      if (player == null) {
        try {
          player = selectPlayer();
        } catch (UserInterruptException | EndOfFileException e) {
          return;
        }
        if (player == null) {
          return;
        }
      }
      player.setCurrentActor(actor);

      emitterFuture = emitterExecutorService.submit(
          new MessageEmitter(player, terminal, lineReader));

      String prePrompt = "";
      String prompt = getPrompt(player);

      while (true) {
        String line;
        try {
          line = lineReader.readLine(prePrompt + prompt);
        } catch (UserInterruptException | EndOfFileException e) {
          break;
        }
        List<String> tokens = CommandLineUtils.tokenize(line);
        prePrompt = AnsiUtils.color("√ ", Ansi.Color.GREEN, true);
        if (!tokens.isEmpty()) {

          Future<CommandResult> commandFuture =
              CommandExecutor.getInstance().submit(actor, player, tokens);
          CommandResult commandResult;
          try {
            commandResult = commandFuture.get();

            if (!commandResult.isSuccessful()) {
              Optional<CommandException> commandException =
                  commandResult.getCommandException();
              if (commandException.isPresent()) {
                player.sendMessage(new Message(player, Message.Style.COMMAND_EXCEPTION,
                                               commandException.get().getMessage()));
              }

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

          if (commandResult.isSuccessful()) {
            Object result = commandResult.getResult();
            if (result != null && !(result instanceof Boolean)) {
              player.sendMessage(new Message(player, Message.Style.INFO, result.toString()));
            }
          }

          if (commandResult.isSuccessful() &&
              commandResult.getCommandClass().isPresent()) {
            Optional<Class<? extends Command>> commandClassOpt = commandResult.getCommandClass();
            Class<? extends Command> commandClass = commandClassOpt.get();

            if (commandClass.equals(ExitCommand.class)) {
              break;
            } else if (commandClass.equals(SwitchPlayerCommand.class)) {
              player = actor.getCurrentPlayer();
              prompt = getPrompt(player);

              emitterFuture.cancel(true);
              emitterFuture = emitterExecutorService.submit(
                 new MessageEmitter(player, terminal, lineReader));
            }
          }

          if (!commandResult.isSuccessful()) {
            prePrompt = AnsiUtils.color("X ", Ansi.Color.RED, true);
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
    } finally {
      if (emitterFuture != null) {
        emitterFuture.cancel(true);
      }
      if (player != null) {
        player.setCurrentActor(null);
      }
    }
  }

  private void emitBanner() {
    if (bannerContent == null) {
      return;
    }
    String bannerToEmit = bannerContent
        .replaceAll("_username_", actor.getUsername())
        .replaceAll("_ipaddress_", getIPAddress());
    terminal.writer().println(bannerToEmit);
  }

  private Player selectPlayer() throws IOException {
    List<Player> permittedPlayers =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername())
        .get().getPlayers().stream()
        .map(id -> Universe.getCurrent().getThing(id, Player.class))
        .filter(p -> p.isPresent())
        .map(p -> p.get())
        .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
        .collect(Collectors.toList());
    terminal.writer().println("Permitted players:");
    for (Player p : permittedPlayers) {
      terminal.writer().printf("  %s\n", p.getName());
    }
    terminal.writer().println("");

    Player chosenPlayer = null;
    if (permittedPlayers.size() == 1) {
      chosenPlayer = permittedPlayers.get(0);
      terminal.writer().printf("Auto-selecting initial player %s\n",
                               chosenPlayer.getName());
    }
    while (chosenPlayer == null) {
      terminal.writer().printf("Select your initial player (exit to disconnect): ");
      String line = lineReader.readLine();
      if (line == null) {
        return null;
      }
      if (line.equals("exit")) {
        return null;
      }

      for (Player p : permittedPlayers) {
        if (p.getName().equals(line)) {
          if (p.getCurrentActor().isPresent()) {
            String occupiedMessage = String.format("Someone is already playing as %s\n",
                                                   p.getName());
            terminal.writer().printf(AnsiUtils.color(occupiedMessage, Ansi.Color.RED, false));
          } else {
            chosenPlayer = p;
          }
          break;
        }
      }
      if (chosenPlayer == null) {
        terminal.writer().printf(AnsiUtils.color("That is not a permitted player\n\n",
                                                   Ansi.Color.RED, false));
      }
    }

    return chosenPlayer;
  }

  private static String getPrompt(Player player) {
    if (player.equals(Player.GOD)) {
      return AnsiUtils.color("# ", Ansi.Color.WHITE, true);
    } else {
      return AnsiUtils.color("$ ", Ansi.Color.WHITE, true);
    }
  }

  private static String joinMessages(Throwable e) {
    return Throwables.getCausalChain(e).stream()
        .map(t -> t.getMessage())
        .collect(Collectors.joining(": "));
  }

  /**
   * Terminates this shell by cancellation / interruption.
   *
   * @return true if the shell was terminated, false if it was not because
   * it already completed or has not yet started
   */
  public boolean terminate() {
    if (future == null) {
      return false;
    }
    return future.cancel(true);
  }
}
