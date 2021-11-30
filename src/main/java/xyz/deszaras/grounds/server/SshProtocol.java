package xyz.deszaras.grounds.server;

import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
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
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;

/**
 * A protocol for connections over SSH, managed by Apache Mina SSHD.<p>
 *
 * Lots of SSH and terminal handling code was lifted from the JLine 3
 * project, particularly from {@code ShellFactoryImpl}.
 */
public class SshProtocol implements Protocol {

  // private static final Logger LOG = LoggerFactory.getLogger(SshProtocol.class);

  private final org.apache.sshd.server.SshServer sshServer;
  private final Server server;

  public static final String DEFAULT_PORT = "4768";


  static boolean isEnabled(Properties serverProperties) {
    return Boolean.valueOf(serverProperties.getProperty("enableSsh", "true"));
  }

  /**
   * Creates a new protocol.
   *
   * @param serverProperties server properties
   * @param server           server
   * @throws IOException if the protocol cannot be created
   * @throws IllegalStateException if a required server property is missing
   */
  public SshProtocol(Properties serverProperties, Server server) throws IOException {
    sshServer = buildSshServer(serverProperties);
    this.server = server;
  }

  private org.apache.sshd.server.SshServer buildSshServer(Properties serverProperties) throws IOException {
    org.apache.sshd.server.SshServer s = org.apache.sshd.server.SshServer.setUpDefaultServer();

    s.setHost(serverProperties.getProperty("host", DEFAULT_HOST));
    s.setPort(Integer.valueOf(serverProperties.getProperty("sshPort", DEFAULT_PORT)));

    String hostKeyFileProperty = serverProperties.getProperty("hostKeyFile");
    if (hostKeyFileProperty == null) {
      throw new IllegalStateException("No hostKeyFile specified");
    }
    Path hostKeyFile = FileSystems.getDefault().getPath(hostKeyFileProperty);
    s.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKeyFile));

    s.setPasswordAuthenticator(
        new SshdPasswordAuthenticator(
            new PasswordAuthenticator(ActorDatabase.INSTANCE)));

    String welcomeBannerFileProperty = serverProperties.getProperty("welcomeBannerFile");
    if (welcomeBannerFileProperty != null) {
      Path welcomeBannerFile = FileSystems.getDefault().getPath(welcomeBannerFileProperty);
      CoreModuleProperties.WELCOME_BANNER.set(s, welcomeBannerFile);
    } else {
      URL defaultWelcomeBannerUrl = Resources.getResource("default_welcome_banner.txt");
      CoreModuleProperties.WELCOME_BANNER.set(s, defaultWelcomeBannerUrl);
    }

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
    public Command createShell(ChannelSession session) throws IOException {
      Actor actor = buildActor(session);

      InetSocketAddress remoteAddress =
          (InetSocketAddress) session.getSessionContext().getRemoteAddress();
      Instant loginTime = Instant.now();
      server.updateActorUponLogin(actor, remoteAddress.getAddress(), loginTime);

      return new ServerShellCommand(actor);
    }

    private Actor buildActor(ChannelSession session) throws IOException {
      if (!session.getSessionContext().isAuthenticated()) {
        throw new IllegalArgumentException("Session is not authenticated");
      }
      // Record must be there, or they couldn't have authenticated
      return server.loadActor(session.getSessionContext().getUsername());
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

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;
    private Future<Integer> shellFuture;

    private ServerShellCommand(Actor actor) {
      this.actor = actor;
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

      shellFuture = server.startShell(actor, virtualTerminal, Optional.of(exitCallback), true);
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
      terminate();
    }

    public void terminate() {
      shellFuture.cancel(true);
    }
  }

  @Override
  public void start() throws IOException {
    sshServer.start();
  }

  public void shutdown() throws IOException {
    sshServer.stop();
  }
}
