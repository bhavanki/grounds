package xyz.deszaras.grounds.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.server.ExitCallback;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.api.ApiServer;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.LoadCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * The multi-user server for the game. Protocols managed by this class
 * determine how connections are made.<p>
 *
 * The server is started through the {@link #start()} method, while
 * it is stopped through the {@link #shutdown()} method. Shutdown is
 * normally triggered, however, through a shutdown command submitted
 * by a logged-in user.<p>
 *
 * An internal executor service is responsible for running shells.
 */
public class Server {

  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  public static final String DEFAULT_ADMIN_THREAD_COUNT = "2";
  public static final String DEFAULT_AUTOSAVE_PERIOD_SECONDS = "300";

  private final ExecutorService shellExecutorService;
  private final String loginBannerContent;
  private final CountDownLatch shutdownLatch;

  private final ScheduledExecutorService adminExecutorService;
  private final long autosavePeriodSeconds;
  private final Multimap<Actor, Shell> openShells;

  private final Set<Protocol> protocols;
  private final ApiServer apiServer;

  private ScheduledFuture<?> autosaveFuture;

  /**
   * Creates a new server.
   *
   * @param serverProperties server properties
   * @throws IOException if the server cannot be created
   * @throws IllegalStateException if a required server property is missing
   */
  protected Server(Properties serverProperties) throws IOException {
    shellExecutorService =
        Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                      .setDaemon(true)
                                      .setNameFormat("grounds-shell-%d")
                                      .build());
    loginBannerContent = readLoginBannerContent(serverProperties);
    shutdownLatch = new CountDownLatch(1);

    loadActorDatabase(serverProperties);

    adminExecutorService = Executors.newScheduledThreadPool(Integer.parseInt(
        serverProperties.getProperty("adminThreadCount", DEFAULT_ADMIN_THREAD_COUNT)),
        new ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("grounds-admin-%d")
            .build());
    autosavePeriodSeconds = Long.parseLong(
        serverProperties.getProperty("autosavePeriodSeconds",
                                     DEFAULT_AUTOSAVE_PERIOD_SECONDS));

    openShells = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    autosaveFuture = null;

    protocols = createProtocols(serverProperties);
    apiServer = createApiServer(serverProperties);
  }

  private String readLoginBannerContent(Properties serverProperties) throws IOException {
    String loginBannerFileProperty = serverProperties.getProperty("loginBannerFile");
    if (loginBannerFileProperty != null) {
      Path loginBannerFile = FileSystems.getDefault().getPath(loginBannerFileProperty);
      return Files.readString(loginBannerFile, StandardCharsets.UTF_8);
    } else {
      return null;
    }
  }

  private void loadActorDatabase(Properties serverProperties) throws IOException {
    String actorDatabaseFileProperty = serverProperties.getProperty("actorDatabaseFile");
    if (actorDatabaseFileProperty == null) {
      throw new IllegalStateException("No actorDatabaseFile specified");
    }
    Path actorDatabaseFile = FileSystems.getDefault().getPath(actorDatabaseFileProperty);
    ActorDatabase.INSTANCE.setPath(actorDatabaseFile);
    ActorDatabase.INSTANCE.load(); // must be present, use SingleUser otherwise
  }

  private Set<Protocol> createProtocols(Properties serverProperties) throws IOException {
    Set<Protocol> ps = new HashSet<>();

    // Slightly naughty: This Server object isn't fully constructed yet but is
    // being passed to protocol objects. This is OK because the protocols won't
    // be used until after Server construction is complete.

    if (SshProtocol.isEnabled(serverProperties)) {
      SshProtocol ssh = new SshProtocol(serverProperties, this);
      ps.add(ssh);
    }
    if (TelnetProtocol.isEnabled(serverProperties)) {
      TelnetProtocol telnet = new TelnetProtocol(serverProperties, this);
      ps.add(telnet);
    }
    if (ps.isEmpty()) {
      throw new IllegalStateException("No protocols are enabled");
    }
    return ImmutableSet.copyOf(ps);
  }

  private ApiServer createApiServer(Properties serverProperties) {
    String apiSocketFile = serverProperties.getProperty("apiSocketFile");
    if (apiSocketFile == null) {
      return null;
    }

    Path apiSocketPath = FileSystems.getDefault().getPath(apiSocketFile);
    return new ApiServer(apiSocketPath);
  }

  /**
   * Gets the set of open shells in the server, by actor. Useful for seeing who
   * is online.
   *
   * @return map of actors and their associated shells
   */
  public Map<Actor, Collection<Shell>> getOpenShells() {
    return ImmutableMap.copyOf(openShells.asMap());
  }

  /**
   * Updates an actor's last known IP address and login time. Implementations
   * should call this after successfully authenticating a user.
   *
   * @param actor         authenticated actor
   * @param remoteAddress actor's current IP address
   * @param loginTime     actor's login time (usually now)
   */
  protected void updateActorUponLogin(Actor actor, InetAddress remoteAddress,
                                      Instant loginTime) {
    String ipAddressString = InetAddresses.toAddrString(remoteAddress);
    ActorDatabase.INSTANCE.updateActorRecord(actor.getUsername(),
        r -> {
          r.setMostRecentIPAddress(ipAddressString);
          r.setLastLoginTime(loginTime);
        });

    try {
      ActorDatabase.INSTANCE.save();
    } catch (IOException e) {
      LOG.warn("Failed to save updated actor record for {} to record " +
               "IP address {} and login time", actor.getUsername(),
               ipAddressString);
    }
    actor.setMostRecentIPAddress(remoteAddress);
    actor.setLastLoginTime(loginTime);
  }

  /**
   * Loads an actor from the actor database. Implementations should call this
   * only after the actor is successfully authenticated, because it's assumed
   * that an actor with the username is actually present. This method clears
   * any lock on the actor, again because it's expected that authentication has
   * already happened (which fails when a lock is still in effect).
   *
   * @param  username actor username
   * @return          loaded actor data
   */
  protected Actor loadActor(String username) {
    Actor actor = new Actor(username);
    ActorDatabase.ActorRecord actorRecord =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername()).get();
    // Clear the account lock if present. (Authentication would have
    // failed if the account were locked.)
    if (actorRecord.getLockedUntil() != null) {
      LOG.info("Clearing lockout for actor {}", actor.getUsername());
      ActorDatabase.INSTANCE.updateActorRecord(actorRecord.getUsername(),
          r -> r.setLockedUntil(null));
      try {
        ActorDatabase.INSTANCE.save();
      } catch (IOException e) {
        LOG.error("Failed to save actor database to clear lockout for {}",
                  actor.getUsername(), e);
      }
    }
    actor.setPreferences(actorRecord.getPreferences());
    return actor;
  }

  /**
   * Starts a shell for the given actor using the given terminal. The returned
   * future provides the shell exit code once the shell task is done, i.e., the
   * actor has disconnected.
   *
   * @param  actor                    actor
   * @param  virtualTerminal          terminal
   * @param  exitCallback             optional callback to use upon shell exit
   * @param  closeTerminalOnShellExit true to close the terminal on exit
   * @return                          future for shell execution
   */
  protected Future<Integer> startShell(Actor actor, Terminal virtualTerminal,
                                       Optional<ExitCallback> exitCallback,
                                       boolean closeTerminalOnShellExit) {
    Shell shell = new Shell(actor, virtualTerminal, shellExecutorService,
                            CommandExecutor.getInstance());
    openShells.put(actor, shell);
    shell.setStartTime(actor.getLastLoginTime());
    shell.setLoginBannerContent(loginBannerContent);

    Callable shellCallable = new Callable<Integer>() {
      @Override
      public Integer call() {
        try {
          LOG.info("Running shell for {} connected from {}", actor.getUsername(),
                   InetAddresses.toAddrString(actor.getMostRecentIPAddress()));
          shell.run();
          LOG.info("Shell for {} exited with exit code {}",
                   actor.getUsername(), shell.getExitCode());
          if (exitCallback.isPresent()) {
            exitCallback.get().onExit(shell.getExitCode());
          }
          return shell.getExitCode();
        } catch (Exception e) {
          LOG.error("Exception thrown by shell for {}",
                    actor.getUsername(), e);
          if (exitCallback.isPresent()) {
            exitCallback.get().onExit(255, e.getMessage());
          }
          return 255;
        } finally {
          openShells.remove(actor, shell);
          if (closeTerminalOnShellExit) {
            try {
              virtualTerminal.close();
            } catch (IOException e) { // NOPMD
              // what can ya do
            }
          }
        }
      }
    };
    Future<Integer> shellFuture = shellExecutorService.submit(shellCallable);
    shell.setFuture(shellFuture);
    return shellFuture;
  }

  /**
   * Starts the server, including all enabled protocols.
   *
   * @throws IOException if the server fails to start
   */
  public void start(File universeFile) throws IOException {
    CommandExecutor.create(apiServer, this);

    if (universeFile != null) {
      CommandExecutor.getInstance()
          .submit(new LoadCommand(Actor.ROOT, Player.GOD, universeFile));
    } else {
      Universe.setCurrent(Universe.VOID);
      Universe.setCurrentFile(null);
    }

    if (autosavePeriodSeconds >= 0L) {
      autosaveFuture = adminExecutorService.scheduleAtFixedRate(
          new AutosaveRunnable(CommandExecutor.getInstance()),
          autosavePeriodSeconds, autosavePeriodSeconds, TimeUnit.SECONDS);
    } else {
      LOG.warn("Autosave disabled");
    }

    for (Protocol p : protocols) {
      p.start();
    }
    if (apiServer != null) {
      apiServer.start();
    }
  }


  private static final long TIMEOUT_ADMIN_EXECUTOR_SERVICE = 10L;

  /**
   * Stops the server, including all enabled protocols.
   *
   * @throws IOException          if there is a problem shutting down the server
   * @throws InterruptedException if the shutdown process is interrupted
   */
  public void shutdown() throws IOException, InterruptedException {
    if (apiServer != null) {
      apiServer.shutdown();
    }
    for (Protocol p : protocols) {
      p.shutdown();
    }

    shellExecutorService.shutdownNow();

    if (autosaveFuture != null) {
      autosaveFuture.cancel(false);
    }
    adminExecutorService.shutdown();
    adminExecutorService.awaitTermination(TIMEOUT_ADMIN_EXECUTOR_SERVICE,
                                          TimeUnit.SECONDS);

    CommandExecutor.getInstance().shutdown();
    LOG.info("Shutdown complete");
  }

  /**
   * Waits for server shutdown to be requested, and then stops the server.
   *
   * @throws IOException if the server fails to stop
   * @throws InterruptedException if the call is interrupted while
   * waiting for the shutdown request
   */
  public void shutdownOnCommand() throws IOException, InterruptedException {
    shutdownLatch.await();
    shutdown();
  }

  /**
   * Request server shutdown.
   */
  public void requestServerShutdown() {
    shutdownLatch.countDown();
  }
}
