package xyz.deszaras.grounds.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
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

public class Server {

  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final String DEFAULT_PORT = "4768";

  private final SshServer sshServer;
  private final ExecutorService shellExecutorService;

  public Server(Properties serverProperties) {
    sshServer = buildSshServer(serverProperties);
    shellExecutorService = Executors.newCachedThreadPool();
  }

  private SshServer buildSshServer(Properties serverProperties) {
    SshServer s = SshServer.setUpDefaultServer();
    s.setHost(serverProperties.getProperty("host", DEFAULT_HOST));
    s.setPort(Integer.valueOf(serverProperties.getProperty("port", DEFAULT_PORT)));
    s.setKeyPairProvider(new SimpleGeneratorHostKeyProvider()); // yuck
    s.setPasswordAuthenticator((username, password, session) -> true); // double yuck
    s.setShellFactory(new ServerShellFactory(shellExecutorService));
    return s;
  }

  private static class ServerShellFactory implements ShellFactory {

    private final ExecutorService shellExecutorService;

    private ServerShellFactory(ExecutorService shellExecutorService) {
      this.shellExecutorService = shellExecutorService;
    }

    @Override
    public Command createShell(ChannelSession session) {
      Actor actor = buildActor(session);
      return new ServerShellCommand(shellExecutorService, actor);
    }

    private static Actor buildActor(ChannelSession session) {
      if (!session.getSessionContext().isAuthenticated()) {
        throw new IllegalArgumentException("Session is not authenticated");
      }
      return new Actor(session.getSessionContext().getUsername());
    }
  }

  private static class ServerShellCommand implements Command {

    private final ExecutorService shellExecutorService;
    private final Shell shell;

    private ExitCallback exitCallback;
    private Future<?> shellFuture;

    private ServerShellCommand(ExecutorService shellExecutorService, Actor actor) {
      this.shellExecutorService = shellExecutorService;
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
    }

    @Override
    public void setErrorStream(OutputStream out) {
      shell.setErr(new OutputStreamWriter(out, StandardCharsets.UTF_8));
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
            shell.run();
            exitCallback.onExit(shell.getExitCode());
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
  }
}
