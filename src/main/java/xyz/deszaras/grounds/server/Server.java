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
import xyz.deszaras.grounds.model.Player;

public class Server {

  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final String DEFAULT_PORT = "4768";

  private final SshServer sshServer;
  private final ExecutorService shellExecutorService;
  private final CountDownLatch shutdownLatch;

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

    String passwordFileProperty = serverProperties.getProperty("passwordFile");
    if (passwordFileProperty == null) {
      throw new IllegalStateException("No passwordFile specified");
    }
    Path passwordFile = FileSystems.getDefault().getPath(passwordFileProperty);
    s.setPasswordAuthenticator(new HashedPasswordAuthenticator(passwordFile));

    s.setShellFactory(new ServerShellFactory());
    return s;
  }

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

  public void start() throws IOException {
    sshServer.start();
  }

  public void shutdown() throws IOException {
    sshServer.stop();
    shellExecutorService.shutdown();
  }

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
