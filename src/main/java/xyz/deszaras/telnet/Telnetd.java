package xyz.deszaras.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

import org.jline.builtins.telnet.Connection;
import org.jline.builtins.telnet.ConnectionData;
import org.jline.builtins.telnet.ConnectionEvent;
import org.jline.builtins.telnet.ConnectionListener;
import org.jline.builtins.telnet.ConnectionManager;
import org.jline.builtins.telnet.PortListener;
import org.jline.builtins.telnet.TelnetIO;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;

/*
 * Based heavily on org.jline.builtins.telnet.Telnet. Its license:
 *
 * Copyright (c) 2002-2017, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */

/**
 * A telnet server.
 */
public class Telnetd {

  private final ShellRunner runner;
  private final int port;
  private final String ip;
  private final int maxConnections;
  private final long warningTimeoutMs;
  private final long disconnectTimeoutMs;
  private final long housekeepingIntervalMs;
  private final String terminalName;

  private PortListener portListener;
  private ConnectionManager connectionManager;

  /**
   * The configuration class for constructing new {@code Telnetd} instances.
   */
  public static class Config {

    public static final String DEFAULT_IP = "127.0.0.1"; // NOPMD
    public static final int DEFAULT_PORT = 23;
    public static final int DEFAULT_MAX_CONNECTIONS = 100;
    public static final long DEFAULT_WARNING_TIMEOUT_MS = 300000L;  // 5 min
    public static final long DEFAULT_DISCONNECT_TIMEOUT_MS = 300000L;  // 5 min
    public static final long DEFAULT_HOUSEKEEPING_INTERVAL_MS = 60000L;  // 1 min
    public static final String DEFAULT_TERMINAL_NAME = "Grounds Telnet";

    private String ip;
    private int port;
    private int maxConnections;
    private long warningTimeoutMs;
    private long disconnectTimeoutMs;
    private long housekeepingIntervalMs;
    private String terminalName;

    /**
     * Creates a new config with default values.
     */
    public Config() {
      ip = DEFAULT_IP;
      port = DEFAULT_PORT;
      maxConnections = DEFAULT_MAX_CONNECTIONS;
      warningTimeoutMs = DEFAULT_WARNING_TIMEOUT_MS;
      disconnectTimeoutMs = DEFAULT_DISCONNECT_TIMEOUT_MS;
      housekeepingIntervalMs = DEFAULT_HOUSEKEEPING_INTERVAL_MS;
      terminalName = DEFAULT_TERMINAL_NAME;
    }

    /**
     * Sets the IP for this config.
     *
     * @param  ip IP address string
     * @return    this
     * @throws NullPointerException if ip is null
     */
    public Config ip(String ip) {
      this.ip = Objects.requireNonNull(ip);
      return this;
    }

    /**
     * Sets the port for this config.
     *
     * @param  port port
     * @return      this
     */
    public Config port(int port) {
      this.port = port;
      return this;
    }

    public Config maxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    public Config warningTimeoutMs(long warningTimeoutMs) {
      this.warningTimeoutMs = warningTimeoutMs;
      return this;
    }

    public Config disconnectTimeoutMs(long disconnectTimeoutMs) {
      this.disconnectTimeoutMs = disconnectTimeoutMs;
      return this;
    }

    public Config housekeepingIntervalMs(long housekeepingIntervalMs) {
      this.housekeepingIntervalMs = housekeepingIntervalMs;
      return this;
    }

    public Config terminalName(String terminalName) {
      this.terminalName = terminalName;
      return this;
    }
  }

  /**
   * A class that runs a telnet shell.
   */
  public interface ShellRunner {
    void runShell(Terminal terminal, ConnectionData connectionData)
        throws IOException;
  }

  /**
   * Creates a new telnet server.
   *
   * @param  config   server configuration
   * @param  runner   runner of new shells
   */
  public Telnetd(Config config, ShellRunner runner) {
    this.runner = runner;

    this.ip = config.ip;
    this.port = config.port;
    this.maxConnections = config.maxConnections;
    this.warningTimeoutMs = config.warningTimeoutMs;
    this.disconnectTimeoutMs = config.disconnectTimeoutMs;
    this.housekeepingIntervalMs = config.housekeepingIntervalMs;
    this.terminalName = config.terminalName;
  }

  /**
   * Gets the IP of this server.
   *
   * @return IP address string
   */
  public String getIp() {
    return ip;
  }

  /**
   * Gets the port of this server.
   *
   * @return port
   */
  public int getPort() {
    return port;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public long getWarningTimeoutMs() {
    return warningTimeoutMs;
  }

  public long getDisconnectTimeoutMs() {
    return disconnectTimeoutMs;
  }

  public long getHousekeepingIntervalMs() {
    return housekeepingIntervalMs;
  }

  public String getTerminalName() {
    return terminalName;
  }

  /**
   * Checks if this server is running.
   *
   * @return true if this server is running
   */
  public boolean isRunning() {
    return portListener != null;
  }

  /**
   * Starts this server.
   *
   * @throws IllegalStateException if the server is already running
   */
  public void start() {
    if (isRunning()) {
      throw new IllegalStateException("telnetd is already running on port " + port);
    }

    connectionManager = new ConnectionManager(maxConnections,
                                              (int) warningTimeoutMs,
                                              (int) disconnectTimeoutMs,
                                              (int) housekeepingIntervalMs,
                                              null, // rest unused
                                              null,
                                              false) {
      @Override
      protected Connection createConnection(ThreadGroup threadGroup, ConnectionData newCD) {
        return new Connection(threadGroup, newCD) {
          TelnetIO telnetIO;

          @Override
          protected void doRun() throws Exception {
            telnetIO = new TelnetIO();
            telnetIO.setConnection(this);
            telnetIO.initIO();

            InputStream in = new InputStream() {
              @Override
              public int read() throws IOException {
                return telnetIO.read();
              }
              @Override
              public int read(byte[] b, int off, int len) throws IOException {
                int r = read();
                if (r >= 0) {
                  b[off] = (byte) r;
                  return 1;
                } else {
                  return -1;
                }
              }
            };
            PrintStream out = new PrintStream(new OutputStream() {
              @Override
              public void write(int b) throws IOException {
                telnetIO.write(b);
              }
              @Override
              public void flush() throws IOException {
                telnetIO.flush();
              }
            });
            Terminal terminal = TerminalBuilder.builder()
                .type(getConnectionData().getNegotiatedTerminalType().toLowerCase())
                .streams(in, out)
                .system(false)
                .name(terminalName)
                .build();
            terminal.setSize(new Size(getConnectionData().getTerminalColumns(),
                                      getConnectionData().getTerminalRows()));
            // terminal.setAttributes(Telnet.this.terminal.getAttributes());
            addConnectionListener(new ConnectionListener() {
              @Override
              public void connectionTerminalGeometryChanged(ConnectionEvent ce) {
                terminal.setSize(new Size(getConnectionData().getTerminalColumns(),
                                          getConnectionData().getTerminalRows()));
                terminal.raise(Signal.WINCH);
              }
            });
            try {
              runner.runShell(terminal, getConnectionData());
            } finally {
              close();
            }
          }

          @Override
          protected void doClose() throws Exception {
            telnetIO.closeOutput();
            telnetIO.closeInput();
          }
        };
      }
    };
    connectionManager.start();

    // TBD: make flood protection (connection queue length) configurable
    portListener = new PortListener("telnetd", ip, port, 10);
    portListener.setConnectionManager(connectionManager);
    portListener.start();
  }

  /**
   * Stops this server.
   *
   * @throws IllegalStateException if the server is not running
   */
  public void stop() {
    if (!isRunning()) {
      throw new IllegalStateException("telnetd is not running");
    }

    portListener.stop();
    portListener = null;
    connectionManager.stop();
    connectionManager = null;
  }
}
