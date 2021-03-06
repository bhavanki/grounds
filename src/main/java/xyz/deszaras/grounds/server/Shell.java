package xyz.deszaras.grounds.server;

import com.google.common.base.Throwables;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.fusesource.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.DestroyCommand;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.command.LookCommand;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.SwitchPlayerCommand;
import xyz.deszaras.grounds.command.YoinkCommand;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
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
  private Instant startTime;
  private String loginBannerContent = null;
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
      emitLoginBanner();

      if (actor.equals(Actor.GUEST)) {
        player = createGuestPlayer();
      }

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
      bringOutPlayer(player);

      emitterFuture = emitterExecutorService.submit(
          new MessageEmitter(player, terminal, lineReader));

      String prePrompt = "";
      String prompt = getPrompt(player);

      autoLook();

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
                String syntaxErrorMessage =
                    String.format("SYNTAX ERROR: %s", joinMessages(commandFactoryException.get()));
                player.sendMessage(new Message(player, Message.Style.COMMAND_FACTORY_EXCEPTION,
                                               syntaxErrorMessage));
                LOG.debug("Command build failed for actor {}", actor.getUsername(),
                          commandFactoryException.get());
              }
            }
          } catch (ExecutionException e) {
            String execErrorMessage = String.format("ERROR: %s", joinMessages(e.getCause()));
            player.sendMessage(new Message(player, Message.Style.EXECUTION_EXCEPTION,
                                           execErrorMessage));
            LOG.debug("Command execution failed for actor {}", actor.getUsername(),
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
              commandResult.getCommand().isPresent()) {
            // javac bug? combining these statements doesn't work
            Optional<Command> commandOpt = commandResult.getCommand();
            Command command = commandOpt.get();

            if (command instanceof ExitCommand) {
              break;
            } else if (command instanceof SwitchPlayerCommand) {
              stowPlayer(player);

              player = ((SwitchPlayerCommand) command).getNewPlayer();
              bringOutPlayer(player);

              prompt = getPrompt(player);

              emitterFuture.cancel(true);
              emitterFuture = emitterExecutorService.submit(
                 new MessageEmitter(player, terminal, lineReader));

              autoLook();
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
        if (actor.equals(Actor.GUEST)) {
          destroyGuestPlayer(player);
        } else {
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

  private static AtomicInteger guestCounter = new AtomicInteger();

  private static String generateGuestName() {
    return String.format("guest%d", guestCounter.incrementAndGet());
  }

  private Player createGuestPlayer() {
    player = new Player(generateGuestName());
    player.setCurrentActor(Actor.GUEST);
    Universe universe = Universe.getCurrent();
    universe.addRole(Role.GUEST, player);
    universe.addThing(player);
    player.setHome(universe.getGuestHomePlace());
    return player;
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  private void destroyGuestPlayer(Player player) {
    Command destroyCommand = new DestroyCommand(Actor.ROOT, Player.GOD, player);
    Future<CommandResult> destroyCommandFuture =
        CommandExecutor.getInstance().submit(destroyCommand);
    try {
      CommandResult destroyCommandResult = destroyCommandFuture.get();

      if (!destroyCommandResult.isSuccessful()) {
        ((Optional<CommandException>) destroyCommandResult.getCommandException())
            .ifPresent(e -> LOG.error(e.getMessage()));
      }
    } catch (ExecutionException e) {
      LOG.error("Destruction of guest {} failed", player.getName(), e.getCause());
    } catch (InterruptedException e) {
      LOG.error("Interrupted while destroying guest {}", player.getName());
    }
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
    Future<CommandResult> bringOutCommandFuture =
        CommandExecutor.getInstance().submit(bringOutCommand);
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
    Future<CommandResult> stowCommandFuture =
        CommandExecutor.getInstance().submit(stowCommand);
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
      if (chosenPlayer.trySetCurrentActor(actor)) {
        terminal.writer().printf("Auto-selecting initial player %s\n",
                                 chosenPlayer.getName());
      } else {
        chosenPlayer = null;
      }
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
          if (p.trySetCurrentActor(actor)) {
            chosenPlayer = p;
          } else {
            String occupiedMessage = String.format("Someone is already playing as %s\n",
                                                   p.getName());
            terminal.writer().printf(AnsiUtils.color(occupiedMessage, Ansi.Color.RED, false));
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

  @SuppressWarnings("PMD.EmptyCatchBlock")
  private void autoLook() throws InterruptedException {
    Future<CommandResult> lookCommandFuture =
        CommandExecutor.getInstance().submit(new LookCommand(actor, player));
    try {
      CommandResult lookCommandResult = lookCommandFuture.get();
      if (lookCommandResult.isSuccessful()) {
        player.sendMessage(new Message(player, Message.Style.INFO,
                                       lookCommandResult.getResult().toString()));
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
