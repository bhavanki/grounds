package xyz.deszaras.grounds.server;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;

/**
 * The multi-user server for the game. Connections happen over SSH,
 * managed by Apache Mina SSHD.<p>
 *
 * The server is started through the {@link #start()} method, while
 * it is stopped through the {@link #shutdown()} method. Shutdown is
 * normally triggered, however, through a shutdown command submitted
 * by a logged-in user.<p>
 *
 * An internal executor service is responsible for running shells.
 */
public class Server {

  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final String DEFAULT_PORT = "4768";

  private final SshServer sshServer;
  private final ExecutorService shellExecutorService;
  private final CountDownLatch shutdownLatch;

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
    shutdownLatch = new CountDownLatch(1);
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

  /**
   * A Mina shell factory for the server. This class is responsible
   * for creating a {@link ServerShellCommand} for each new session.
   * The actor for the shell corresponds to the authenticated user.
   */
  private class ServerShellFactory implements ShellFactory {

    private ServerShellFactory() {
    }

    @Override
    public Command createShell(ChannelSession session) {
      Actor actor = buildActor(session);
      return new ServerShellCommand(actor);
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

    private final Shell shell;

    private ExitCallback exitCallback;
    private Future<?> shellFuture;

    private ServerShellCommand(Actor actor) {
      shell = new Shell(actor);
      shellFuture = null;
    }

    @Override
    public void setInputStream(InputStream in) {
      shell.setIn(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    @Override
    public void setOutputStream(OutputStream out) {
      shell.setOut(new OutputStreamWriter(out, StandardCharsets.UTF_8));
      // shell.setOut(new OutputStreamWriter(new NlCorrectingOutputStream(out), StandardCharsets.UTF_8));
    }

    @Override
    public void setErrorStream(OutputStream err) {
      shell.setErr(new OutputStreamWriter(err, StandardCharsets.UTF_8));
      // shell.setErr(new OutputStreamWriter(new NlCorrectingOutputStream(err), StandardCharsets.UTF_8));
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
      this.exitCallback = exitCallback;
    }

    @Override
    public void start(ChannelSession session, Environment env) {
      Runnable shellRunnable = new Runnable() {
        @Override
        public void run() {
          try {
            shell.setPlayer(Player.GOD);  // temporary
            shell.run();
            exitCallback.onExit(shell.getExitCode());
            if (shell.exitedWithShutdown()) {
              shutdownLatch.countDown();
            }
          } catch (Exception e) {
            exitCallback.onExit(255, e.getMessage());
          }
        }
      };
      shellFuture = shellExecutorService.submit(shellRunnable);
    }

    @Override
    public void destroy(ChannelSession session) {
      shellFuture.cancel(true);
    }
  }

  /**
   * Starts the server.
   *
   * @throws IOException if the server fails to start
   */
  public void start() throws IOException {
    sshServer.start();
  }

  /**
   * Stops the server, also shutting down execution of shells
   * and commands.
   *
   * @throws IOException if the server fails to stop
   */
  public void shutdown() throws IOException {
    sshServer.stop();
    shellExecutorService.shutdown();
    CommandExecutor.INSTANCE.shutdown();
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

  // https://github.com/bigpuritz/javaforge-blog/blob/master/sshd-daemon-demo/src/main/java/net/javaforge/blog/sshd/InAppShellFactory.java
  private static class NlCorrectingOutputStream extends FilterOutputStream {

    private static final boolean MAC = System.getProperty("os.name").startsWith("Mac OS X");

    private NlCorrectingOutputStream(OutputStream out) {
      super(out);
    }

    @Override
    public void write(int i) throws IOException {
      if (MAC && i == '\n') {
        super.write('\r');
      }

      super.write(i);
    }
  }
}
