package xyz.deszaras.grounds.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Attributes.OutputFlag;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;

/**
 * The multi-user server for the game. Connections happen over SSH,
 * managed by Apache Mina SSHD.<p>
 *
 * The server is started through the {@link #start()} method, while
 * it is stopped through the {@link #shutdown()} method. Shutdown is
 * normally triggered, however, through a shutdown command submitted
 * by a logged-in user.<p>
 *
 * An internal executor service is responsible for running shells.<p>
 *
 * Lots of SSH and terminal handling code was lifted from the JLine 3
 * project, particularly from {@code ShellFactoryImpl}.
 */
public class Server {

  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  public static final String DEFAULT_HOST = "127.0.0.1"; // NOPMD
  public static final String DEFAULT_PORT = "4768";

  private final SshServer sshServer;
  private final ExecutorService shellExecutorService;
  private final String bannerContent;
  private final CountDownLatch shutdownLatch;

  private final Set<Actor> connectedActors;
  private final Cache<Actor, Shell> openShells;

  /**
   * Creates a new server.
   *
   * @param serverProperties server properties
   * @throws IOException if the server cannot be created
   * @throws IllegalStateException if a required server property is missing
   */
  public Server(Properties serverProperties) throws IOException {
    sshServer = buildSshServer(serverProperties);
    shellExecutorService = Executors.newCachedThreadPool();
    bannerContent = readBannerContent(serverProperties);
    shutdownLatch = new CountDownLatch(1);

    connectedActors = new HashSet<>();
    openShells = CacheBuilder.newBuilder().weakValues().build();
  }

  public Map<Actor, Shell> getOpenShells() {
    return ImmutableMap.copyOf(openShells.asMap());
  }

  private SshServer buildSshServer(Properties serverProperties) throws IOException {
    SshServer s = SshServer.setUpDefaultServer();

    s.setHost(serverProperties.getProperty("host", DEFAULT_HOST));
    s.setPort(Integer.valueOf(serverProperties.getProperty("port", DEFAULT_PORT)));

    String hostKeyFileProperty = serverProperties.getProperty("hostKeyFile");
    if (hostKeyFileProperty == null) {
      throw new IllegalStateException("No hostKeyFile specified");
    }
    Path hostKeyFile = FileSystems.getDefault().getPath(hostKeyFileProperty);
    s.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKeyFile));

    String actorDatabaseFileProperty = serverProperties.getProperty("actorDatabaseFile");
    if (actorDatabaseFileProperty == null) {
      throw new IllegalStateException("No actorDatabaseFile specified");
    }
    Path actorDatabaseFile = FileSystems.getDefault().getPath(actorDatabaseFileProperty);
    ActorDatabase.INSTANCE.setPath(actorDatabaseFile);
    ActorDatabase.INSTANCE.load(); // must be present, use SingleUser otherwise
    s.setPasswordAuthenticator(new HashedPasswordAuthenticator(ActorDatabase.INSTANCE));

    s.setShellFactory(new ServerShellFactory());
    return s;
  }

  private String readBannerContent(Properties serverProperties) throws IOException {
    String bannerFileProperty = serverProperties.getProperty("bannerFile");
    if (bannerFileProperty != null) {
      Path bannerFile = FileSystems.getDefault().getPath(bannerFileProperty);
      return Files.readString(bannerFile, StandardCharsets.UTF_8);
    } else {
      return null;
    }
  }

  /**
   * A Mina shell factory for the server. This class is responsible
   * for creating a {@link ServerShellCommand} for each new session.
   * The actor for the shell corresponds to the authenticated user.
   */
  private class ServerShellFactory implements ShellFactory {

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String LOCALHOST = "127.0.0.1";

    private ServerShellFactory() {
    }

    @Override
    public Command createShell(ChannelSession session) throws IOException {
      Actor actor = buildActor(session);

      InetSocketAddress remoteAddress =
          (InetSocketAddress) session.getSessionContext().getRemoteAddress();

      // If actor is root, only permit connecting from 127.0.0.1.
      if (actor.equals(Actor.ROOT) &&
         !(remoteAddress.getAddress().getHostAddress().equals(LOCALHOST))) {
        throw new IOException("root may only connect from localhost");
      }

      // If the actor is already connected, reject.
      synchronized (connectedActors) {
        if (connectedActors.contains(actor)) {
          throw new IOException(actor.getUsername() + " is already connected");
        }
        connectedActors.add(actor);
      }

      // Remember the actor's IP address.
      String ipAddressString = InetAddresses.toAddrString(remoteAddress.getAddress());
      ActorDatabase.INSTANCE.updateActorRecord(actor.getUsername(),
          r -> r.setMostRecentIPAddress(ipAddressString));
      try {
        ActorDatabase.INSTANCE.save();
      } catch (IOException e) {
        LOG.warn("Failed to save updated actor record for {} to record IP address {}",
                 actor.getUsername(), ipAddressString);
      }

      return new ServerShellCommand(actor, ipAddressString);
    }

    private Actor buildActor(ChannelSession session) {
      if (!session.getSessionContext().isAuthenticated()) {
        throw new IllegalArgumentException("Session is not authenticated");
      }
      return new Actor(session.getSessionContext().getUsername());
    }
  }

  /**
   * A Mina command (shell, really) for the server, assigned to an
   * authenticated user / actor. This object wraps a native
   * {@link #Shell} for the actor, and this object bridges the
   * streams between the two of them.
   */
  private class ServerShellCommand implements Command {

    private final Actor actor;
    private final String ipAddressString;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;
    private Future<?> shellFuture;

    private ServerShellCommand(Actor actor, String ipAddressString) {
      this.actor = actor;
      this.ipAddressString = ipAddressString;
      shellFuture = null;
    }

    @Override
    public void setInputStream(InputStream in) {
      this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
      this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
      this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
      this.exitCallback = exitCallback;
    }

    @Override
    public void start(ChannelSession session, Environment env) throws IOException {
      if (in == null || out == null || err == null) {
        throw new IllegalStateException("I/O is not connected!");
      }

      Terminal virtualTerminal = TerminalBuilder.builder()
          .name("Grounds SSH")
          .type(env.getEnv().get("TERM"))
          .encoding(StandardCharsets.UTF_8)
          .system(false)
          .streams(in, out)
          .build();
      virtualTerminal.setSize(new Size(Integer.parseInt(env.getEnv().get("COLUMNS")),
                                       Integer.parseInt(env.getEnv().get("LINES"))));
      processEnvPtyModes(env, virtualTerminal);
      env.addSignalListener((channel, signal) -> {
          virtualTerminal.setSize(new Size(Integer.parseInt(env.getEnv().get("COLUMNS")),
                                           Integer.parseInt(env.getEnv().get("LINES"))));
          virtualTerminal.raise(Terminal.Signal.WINCH);
        }, Signal.WINCH);

      Shell shell = new Shell(actor, virtualTerminal, ipAddressString);
      openShells.put(actor, shell);
      shell.setBannerContent(bannerContent);

      Future<?> emitterFuture = shellExecutorService.submit(
          new MessageEmitter(actor, virtualTerminal,
                             shell.getLineReader()));
      Runnable shellRunnable = new Runnable() {
        @Override
        public void run() {
          try {
            LOG.info("Running shell for {} connected from {}", actor.getUsername(),
                     ipAddressString);
            shell.run();
            LOG.info("Shell for {} exited with exit code {}",
                     actor.getUsername(), shell.getExitCode());
            exitCallback.onExit(shell.getExitCode());
          } catch (Exception e) {
            LOG.error("Exception thrown by shell for {}",
                      actor.getUsername(), e);
            exitCallback.onExit(255, e.getMessage());
          } finally {
            emitterFuture.cancel(true);
            openShells.invalidate(actor);
            try {
              virtualTerminal.close();
            } catch (IOException e) { // NOPMD
              // what can ya do
            }
          }
        }
      };
      shellFuture = shellExecutorService.submit(shellRunnable);
    }

    /**
     * Does magical stuff with the control characters for the terminal,
     * based on the environment's PTY modes. If I were to claim I
     * understand this, I'd be lying.<p>
     *
     * Possibly related note: Turn off "bracketed paste" in your
     * terminal. Maybe it's related to this stuff here.
     *
     * @param env SSH environment
     * @param terminal JLine terminal
     */
    private void processEnvPtyModes(Environment env, Terminal terminal) {
      Attributes attr = terminal.getAttributes();
      for (Map.Entry<PtyMode, Integer> e : env.getPtyModes().entrySet()) {
          switch (e.getKey()) {
              case VINTR:
                  attr.setControlChar(ControlChar.VINTR, e.getValue());
                  break;
              case VQUIT:
                  attr.setControlChar(ControlChar.VQUIT, e.getValue());
                  break;
              case VERASE:
                  attr.setControlChar(ControlChar.VERASE, e.getValue());
                  break;
              case VKILL:
                  attr.setControlChar(ControlChar.VKILL, e.getValue());
                  break;
              case VEOF:
                  attr.setControlChar(ControlChar.VEOF, e.getValue());
                  break;
              case VEOL:
                  attr.setControlChar(ControlChar.VEOL, e.getValue());
                  break;
              case VEOL2:
                  attr.setControlChar(ControlChar.VEOL2, e.getValue());
                  break;
              case VSTART:
                  attr.setControlChar(ControlChar.VSTART, e.getValue());
                  break;
              case VSTOP:
                  attr.setControlChar(ControlChar.VSTOP, e.getValue());
                  break;
              case VSUSP:
                  attr.setControlChar(ControlChar.VSUSP, e.getValue());
                  break;
              case VDSUSP:
                  attr.setControlChar(ControlChar.VDSUSP, e.getValue());
                  break;
              case VREPRINT:
                  attr.setControlChar(ControlChar.VREPRINT, e.getValue());
                  break;
              case VWERASE:
                  attr.setControlChar(ControlChar.VWERASE, e.getValue());
                  break;
              case VLNEXT:
                  attr.setControlChar(ControlChar.VLNEXT, e.getValue());
                  break;
              /*
              case VFLUSH:
                  attr.setControlChar(ControlChar.VMIN, e.getValue());
                  break;
              case VSWTCH:
                  attr.setControlChar(ControlChar.VTIME, e.getValue());
                  break;
              */
              case VSTATUS:
                  attr.setControlChar(ControlChar.VSTATUS, e.getValue());
                  break;
              case VDISCARD:
                  attr.setControlChar(ControlChar.VDISCARD, e.getValue());
                  break;
              case ECHO:
                  attr.setLocalFlag(LocalFlag.ECHO, e.getValue() != 0);
                  break;
              case ICANON:
                  attr.setLocalFlag(LocalFlag.ICANON, e.getValue() != 0);
                  break;
              case ISIG:
                  attr.setLocalFlag(LocalFlag.ISIG, e.getValue() != 0);
                  break;
              case ICRNL:
                  attr.setInputFlag(InputFlag.ICRNL, e.getValue() != 0);
                  break;
              case INLCR:
                  attr.setInputFlag(InputFlag.INLCR, e.getValue() != 0);
                  break;
              case IGNCR:
                  attr.setInputFlag(InputFlag.IGNCR, e.getValue() != 0);
                  break;
              case OCRNL:
                  attr.setOutputFlag(OutputFlag.OCRNL, e.getValue() != 0);
                  break;
              case ONLCR:
                  attr.setOutputFlag(OutputFlag.ONLCR, e.getValue() != 0);
                  break;
              case ONLRET:
                  attr.setOutputFlag(OutputFlag.ONLRET, e.getValue() != 0);
                  break;
              case OPOST:
                  attr.setOutputFlag(OutputFlag.OPOST, e.getValue() != 0);
                  break;
          }
      }
      terminal.setAttributes(attr);
    }

    @Override
    public void destroy(ChannelSession session) {
      shellFuture.cancel(true);

      synchronized (connectedActors) {
        connectedActors.remove(new Actor(session.getSessionContext().getUsername()));
      }
    }
  }

  /**
   * Starts the server.
   *
   * @throws IOException if the server fails to start
   */
  public void start() throws IOException {
    CommandExecutor.create(this);

    sshServer.start();
  }

  /**
   * Stops the server, also shutting down execution of shells
   * and commands.
   *
   * @throws IOException if the server fails to stop
   */
  public void shutdown() throws IOException {
    shellExecutorService.shutdownNow();
    sshServer.stop();
    CommandExecutor.getInstance().shutdown();
    LOG.info("Shutdown complete");
  }

  /**
   * Waits for server shutdown to be requested, and then
   * stops the server.
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
