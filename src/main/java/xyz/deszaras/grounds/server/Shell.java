package xyz.deszaras.grounds.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.fusesource.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandCompleter;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.LookCommand;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.PreferenceCommand;
import xyz.deszaras.grounds.command.SwitchPlayerCommand;
import xyz.deszaras.grounds.command.YoinkCommand;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;

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
  private final CommandExecutor commandExecutor;
  private final LineReader lineReader;

  private Player player = null;
  private Instant startTime;
  private String loginBannerContent = null;
  private Future<Integer> future = null;
  private int exitCode = 0;

  /**
   * Creates a new shell.
   *
   * @param actor actor using the shell
   * @param terminal actor's terminal
   * @param emitterExecutorService executor for submitting messages
   * @param commandExecutor executor for commands run by the shell itself
   */
  public Shell(Actor actor, Terminal terminal, ExecutorService emitterExecutorService,
               CommandExecutor commandExecutor) {
    this(actor, terminal, emitterExecutorService, commandExecutor, null);
  }

  @VisibleForTesting
  Shell(Actor actor, Terminal terminal, ExecutorService emitterExecutorService,
        CommandExecutor commandExecutor, LineReader lineReader) {
    this.actor = actor;
    this.terminal = terminal;
    this.emitterExecutorService = emitterExecutorService;
    this.commandExecutor = commandExecutor;

    if (lineReader != null) {
      this.lineReader = lineReader;
    } else {
      this.lineReader = LineReaderBuilder.builder()
          .terminal(terminal)
          .parser(new DefaultParser()
                  .eofOnEscapedNewLine(true)
                  .eofOnUnclosedQuote(true))
          .completer(new CommandCompleter())
          .option(LineReader.Option.CASE_INSENSITIVE, true)
          .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
          .build();
    }
  }

  /**
   * Gets the actor using this shell
   *
   * @return actor
   */
  public Actor getActor() {
    return actor;
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
   * Gets the start time for the shell.
   *
   * @return start time
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Gets the start time for the shell.
   *
   * @param startTime start time
   */
  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  /**
   * Gets the player for the shell.
   *
   * @return player
   */
  public Optional<Player> getPlayer() {
    return Optional.ofNullable(player);
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
   * Sets the login banner content for the shell.
   *
   * @param loginBannerContent banner content
   */
  public void setLoginBannerContent(String loginBannerContent) {
    this.loginBannerContent = loginBannerContent;
  }

  /**
   * Sets the future for the task that executes this shell.
   *
   * @param future future for this shell
   */
  public void setFuture(Future<Integer> future) {
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

    // The flag is an InheritedThreadLocal, so it only affects this shell.
    Ansi.setEnabled(actor.getBooleanPreference(Actor.PREFERENCE_ANSI));
    boolean hidePrompt = actor.getBooleanPreference(Actor.PREFERENCE_HIDE_PROMPT);

    Future<?> emitterFuture = null;
    Concierge concierge = new Concierge(Universe.getCurrent(), commandExecutor);

    try {
      emitLoginBanner();

      if (actor.equals(Actor.GUEST)) {
        player = concierge.buildGuestPlayer();
      }

      if (player == null) {
        PlayerSelection playerSelection =
            new PlayerSelection(lineReader, terminal.writer(), actor, Universe.getCurrent());
        try {
          player = playerSelection.call();
        } catch (UserInterruptException | EndOfFileException e) {
          return;
        }
        if (player == null) {
          return;
        }
      }
      bringOutPlayer(player);
      commandExecutor.getCommandEventBus().post(new ArrivalEvent(player));

      emitterFuture = emitterExecutorService.submit(
          new MessageEmitter(player, terminal, lineReader));

      String prePrompt = "";
      String prompt = getPrompt(player);

      autoLook();

      while (true) {
        try {
          lineReader.readLine(hidePrompt ? (String) null : prePrompt + prompt);
        } catch (UserInterruptException | EndOfFileException e) {
          exitCode = 1;
          break;
        }

        List<String> tokens = lineReader.getParsedLine().words();
        boolean success = true;
        if (!tokens.isEmpty() && !String.join("", tokens).isEmpty()) {

          Future<CommandResult<?>> commandFuture = commandExecutor.submit(actor, player, tokens);
          CommandResult<?> commandResult;
          try {
            commandResult = commandFuture.get();
          } catch (ExecutionException e) {
            LOG.debug("Command execution failed for actor {}", actor.getUsername(),
                      e.getCause());
            commandResult = new CommandResult<>(new CommandException("Command failed", e.getCause()));
          }

          if (!commandResult.isSuccessful()) {
            player.sendMessage(commandResult.getFailureMessage(player));
            if (commandResult.getCommandFactoryException().isPresent()) {
              LOG.debug("Command build failed for actor {}", actor.getUsername(),
                        commandResult.getCommandFactoryException().get());
            }
          } else {
            Object result = commandResult.getResult();
            if (result != null && !(result instanceof Boolean)) {
              player.sendMessage(new Message(player, Message.Style.INFO, result.toString()));
            }
          }

          if (commandResult.isSuccessful() &&
              commandResult.getCommand().isPresent()) {
            // javac bug? combining these statements doesn't work
            Optional<Command> commandOpt = commandResult.getCommand();
            Command command = commandOpt.get();

            if (command instanceof ExitCommand) {
              break;
            } else if (command instanceof SwitchPlayerCommand) {
              commandExecutor.getCommandEventBus().post(new DepartureEvent(player));
              stowPlayer(player);

              player = ((SwitchPlayerCommand) command).getNewPlayer();
              bringOutPlayer(player);
              commandExecutor.getCommandEventBus().post(new ArrivalEvent(player));

              prompt = getPrompt(player);

              emitterFuture.cancel(true);
              emitterFuture = emitterExecutorService.submit(
                 new MessageEmitter(player, terminal, lineReader));

              autoLook();
            } else if (command instanceof PreferenceCommand) {
              // refresh shell based on updated preferences
              Ansi.setEnabled(actor.getBooleanPreference(Actor.PREFERENCE_ANSI));
              hidePrompt = actor.getBooleanPreference(Actor.PREFERENCE_HIDE_PROMPT);
            }
          }
          success = commandResult.isSuccessful();
        }

        if (success) {
          prePrompt = AnsiUtils.color("√ ", Ansi.Color.GREEN, true);
        } else {
          prePrompt = AnsiUtils.color("X ", Ansi.Color.RED, true);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace(err);
      out.println("Interrupted! Exiting");
    } finally {
      if (emitterFuture != null) {
        emitterFuture.cancel(true);
      }
      if (player != null) {
        commandExecutor.getCommandEventBus().post(new DepartureEvent(player));
        if (actor.equals(Actor.GUEST)) {
          concierge.destroyGuestPlayer(player);
        } else {
          player.setCurrentActor(null);
          try {
            stowPlayer(player);
          } catch (InterruptedException e) {
            e.printStackTrace(err);
            out.println("Interrupted stowing player");
          }
        }
      }
    }
  }

  private void emitLoginBanner() {
    if (loginBannerContent == null) {
      return;
    }
    String loginBannerToEmit = loginBannerContent
        .replaceAll("_username_", actor.getUsername())
        .replaceAll("_ipaddress_", getIPAddress());
    terminal.writer().println(loginBannerToEmit);
  }

  private void bringOutPlayer(Player player) throws InterruptedException {
    Optional<Place> home;
    try {
      home = player.getHome();
    } catch (MissingThingException e) {
      home = Optional.empty();
    }
    if (home.isEmpty()) {
      home = Optional.of(Universe.getCurrent().getOriginPlace());
    }
    LOG.debug("Bringing out player {} to home {}", player.getName(),
              home.get().getName());
    Command bringOutCommand = new YoinkCommand(Actor.ROOT, Player.GOD, player,
                                               home.get());
    Future<CommandResult> bringOutCommandFuture = commandExecutor.submit(bringOutCommand);
    try {
      CommandResult bringOutCommandResult = bringOutCommandFuture.get();

      if (!bringOutCommandResult.isSuccessful()) {
        ((Optional<CommandException>) bringOutCommandResult.getCommandException())
            .ifPresent(e -> LOG.error(e.getMessage()));
      }
    } catch (ExecutionException e) {
      LOG.error("Bringing out player {} failed", player.getName(), e.getCause());
    }
  }

  private void stowPlayer(Player player) throws InterruptedException {
    Place origin = Universe.getCurrent().getOriginPlace();
    LOG.debug("Stowing player {} at origin {}", player.getName(),
              origin.getName());
    Command stowCommand = new YoinkCommand(Actor.ROOT, Player.GOD, player, origin);
    Future<CommandResult> stowCommandFuture = commandExecutor.submit(stowCommand);
    try {
      CommandResult stowCommandResult = stowCommandFuture.get();

      if (!stowCommandResult.isSuccessful()) {
        ((Optional<CommandException>) stowCommandResult.getCommandException())
            .ifPresent(e -> LOG.error(e.getMessage()));
      }
    } catch (ExecutionException e) {
      LOG.error("Stowing player {} failed", player.getName(), e.getCause());
    }
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  private void autoLook() throws InterruptedException {
    Future<CommandResult<String>> lookCommandFuture =
        commandExecutor.submit(new LookCommand(actor, player));
    try {
      CommandResult<String> lookCommandResult = lookCommandFuture.get();
      if (lookCommandResult.isSuccessful()) {
        player.sendMessage(new Message(player, Message.Style.INFO,
                                       lookCommandResult.getResult()));
      }
    } catch (ExecutionException e) {
      // oh well
    }
  }

  private static String getPrompt(Player player) {
    if (player.equals(Player.GOD)) {
      return AnsiUtils.color("# ", Ansi.Color.WHITE, true);
    } else {
      return AnsiUtils.color("$ ", Ansi.Color.WHITE, true);
    }
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
