package xyz.deszaras.grounds.server;

import com.google.common.net.InetAddresses;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.telnet.ConnectionData;
import xyz.deszaras.telnet.Telnetd;
import xyz.deszaras.telnet.Telnetd.ShellRunner;

/**
 * A protocol for connections over telnet.
 */
public class TelnetProtocol implements Protocol {

  private static final Logger LOG = LoggerFactory.getLogger(TelnetProtocol.class);

  public static final String DEFAULT_PORT = "4769";
  public static final int DEFAULT_LOGIN_ATTEMPTS = 5;

  static boolean isEnabled(Properties serverProperties) {
    return Boolean.valueOf(serverProperties.getProperty("enableTelnet", "false"));
  }

  private final Telnetd telnetServer;
  private final Server server;
  private final String welcomeBanner;

  /**
   * Creates a new protocol.
   *
   * @param serverProperties server properties
   * @param server           server
   * @throws IOException if the protocol cannot be created
   * @throws IllegalStateException if a required server property is missing
   */
  public TelnetProtocol(Properties serverProperties, Server server) throws IOException {
    telnetServer = buildTelnetServer(serverProperties);
    this.server = server;

    String welcomeBannerFileProperty = serverProperties.getProperty("welcomeBannerFile");
    if (welcomeBannerFileProperty != null) {
      Path welcomeBannerFile = FileSystems.getDefault().getPath(welcomeBannerFileProperty);
      welcomeBanner = Files.readString(welcomeBannerFile);
    } else {
      URL defaultWelcomeBannerUrl = Resources.getResource("default_welcome_banner.txt");
      welcomeBanner = Resources.toString(defaultWelcomeBannerUrl, StandardCharsets.UTF_8);
    }
  }

  private Telnetd buildTelnetServer(Properties serverProperties)
      throws IOException {
    String host = serverProperties.getProperty("host", DEFAULT_HOST);
    int port = Integer.valueOf(serverProperties.getProperty("telnetPort", DEFAULT_PORT));
    Telnetd.Config config = new Telnetd.Config()
        .ip(host)
        .port(port);

    PasswordAuthenticator authenticator = new PasswordAuthenticator(ActorDatabase.INSTANCE);

    // Terminal baseTerminal = TerminalBuilder.builder()
    //     .name("Grounds Telnet")
    //     .encoding(StandardCharsets.UTF_8)
    //     .build();

    return new Telnetd(config, new ServerShellRunner(authenticator));
  }

  private static final Pattern CONNECT_PATTERN =
      Pattern.compile("^connect ([^ ]+) ([^ ]+)$");

  /**
   * A runner for server shells. This class is responsible for authenticating a
   * user, starting their shell, and waiting for the shell's completion.
   */
  private class ServerShellRunner implements ShellRunner {

    private final PasswordAuthenticator authenticator;
    private Actor actor;
    private Future<Integer> shellFuture;

    private ServerShellRunner(PasswordAuthenticator authenticator) {
      this.authenticator = authenticator;
    }

    @Override
    public void runShell(Terminal terminal, ConnectionData connectionData)
        throws IOException {
      terminal.writer().println(welcomeBanner);
      InetAddress remoteAddress = connectionData.getInetAddress();

      try {
        actor = login(terminal, remoteAddress);
        if (actor == null) {
          return;
        }

        Instant loginTime = Instant.now();
        server.updateActorUponLogin(actor, remoteAddress, loginTime);

      // processEnvPtyModes(env, virtualTerminal);

        shellFuture = server.startShell(actor, terminal, Optional.empty(), false);
        try {
          shellFuture.get();
        } catch (InterruptedException e) {
          // shell was canceled
        } catch (ExecutionException e) {
          LOG.error("Shell failed", e);
        }
      } catch (InterruptedException e) {
        // return normally
      }
    }

    private Actor login(Terminal terminal, InetAddress remoteAddress)
        throws IOException, InterruptedException {
      LineReader lineReader = LineReaderBuilder.builder()
          .terminal(terminal)
          .build();

      String username = "";
      boolean auth = false;
      for (int i = 0; i < DEFAULT_LOGIN_ATTEMPTS; i++) {
        terminal.writer().println("Log in with 'connect <username> <password>'");
        terminal.writer().println("  or use 'exit' to disconnect");
        terminal.writer().println("");
        String line = lineReader.readLine("> ");
        if ("exit".equals(line)) {
          return null;
        }
        Matcher connectMatcher = CONNECT_PATTERN.matcher(line);
        if (!connectMatcher.matches()) {
          terminal.writer().println("Invalid syntax");
        } else {
          username = connectMatcher.group(1);
          String password = connectMatcher.group(2);
          auth = authenticator.authenticate(username, password,
                                            InetAddresses.toAddrString(remoteAddress));
          if (auth) {
            break;
          }
          terminal.writer().println("Invalid username or password");
        }
        Thread.currentThread().sleep((long) Math.pow(2000.0, (double) i));
      }
      if (!auth) {
        return null;
      }

      // Record must be there, or they couldn't have authenticated
      return server.loadActor(username);
    }
  }

  @Override
  public void start() throws IOException {
    try {
      telnetServer.start();
    } catch (Exception e) {
      throw new IOException("Failed to start telnet", e);
    }
  }

  @Override
  public void shutdown() throws IOException {
    try {
      telnetServer.stop();
    } catch (Exception e) {
      throw new IOException("Failed to stop telnet", e);
    }
  }
}
