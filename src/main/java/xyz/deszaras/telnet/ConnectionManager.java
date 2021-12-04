package xyz.deszaras.telnet;

import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jline.builtins.telnet.ConnectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Based heavily on org.jline.builtins.telnet.ConnectionManager. Its license:
 *
 * Copyright (c) 2002-2018, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Manager for connections, which mostly exists to perform housekeeping tasks
 * like enforcing idle timeouts.
 */
public abstract class ConnectionManager implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

  private final ExecutorService connectionExecutor;
  private final Map<Connection,Future<?>> openConnections;

  private final int maxConnections;
  private final long warningTimeoutMs;
  private final long disconnectTimeoutMs;
  private final long housekeepingIntervalMs;

  private final ConnectionFilter connectionFilter;
  private final String loginShell;
  private final boolean lineMode;

  private Thread thread;

  /**
   * Creates a new manager.
   *
   * @param  maxConnections         maximum number of connections to permit
   * @param  warningTimeoutMs       timeout before an idle connection is warned
   * @param  disconnectTimeoutMs    timeout before an idle connection is disconnected
   * @param  housekeepingIntervalMs interval for housekeeping tasks
   * @param  connectionFilter       optional connection filter
   * @param  loginShell             name of login shell (information only)
   * @param  lineMode               whether line mode is active
   */
  public ConnectionManager(int maxConnections, long warningTimeoutMs,
                           long disconnectTimeoutMs, int housekeepingIntervalMs,
                           ConnectionFilter connectionFilter,
                           String loginShell, boolean lineMode) {
    connectionExecutor = Executors.newCachedThreadPool();
    openConnections = new HashMap<>();

    this.maxConnections = maxConnections;
    this.warningTimeoutMs = warningTimeoutMs;
    this.disconnectTimeoutMs = disconnectTimeoutMs;
    this.housekeepingIntervalMs = housekeepingIntervalMs;

    this.connectionFilter = connectionFilter;
    this.loginShell = loginShell;
    this.lineMode = lineMode;
  }

  /**
   * Starts the housekeeping thread for this connection manager.
   *
   * @throws IllegalStateException if housekeeping is already started
   */
  public synchronized void start() {
    // TODO prevent reuse / restart
    if (thread != null) {
      throw new IllegalStateException("already started");
    }
    // TODO use executor instead
    thread = new Thread(this);
    thread.start();
  }

  /**
   * Stops the housekeeping thread for this connection manager. This causes all
   * connections to be closed.
   */
  public synchronized void stop() {
    if (thread != null) {
      thread.interrupt();
      thread = null;
    }
  }

  /**
   * Establishes a new open connection and starts a thread for it. This hands
   * off control of the socket; callers should not attempt to close it later.
   *
   * @param insock socket for incoming connection
   * @throws IllegalStateException if the connection should be refused
   */
  public void startConnection(Socket insock) {
    if (connectionFilter == null ||
        connectionFilter.isAllowed(insock.getInetAddress())) {

      ConnectionData newCD = new ConnectionData(insock, this);
      newCD.setLoginShell(loginShell);
      newCD.setLineMode(lineMode);

      synchronized(openConnections) {
        if (openConnections.size() < maxConnections) {
          Connection conn = createConnection(newCD);
          openConnections.put(conn, connectionExecutor.submit(conn));
          LOG.info("Connection to {} established", newCD.getHostAddress());
        } else {
          throw new IllegalStateException("The maximum number of connections has been reached");
        }
      }
    } else {
      throw new IllegalStateException("Filter blocked incoming connection");
    }
  }

  /**
   * Creates a new connection object based on the given data. Subclasses must
   * implement this factory method.
   *
   * @param  newCD connection data
   * @return       connection
   */
  protected abstract Connection createConnection(ConnectionData newCD);

  /**
   * Runs the housekeeping thread for this connection manager. When this method
   * exits, all connections are closed.
   */
  public void run() {
    try {
      while (true) {
        checkOpenConnections();
        Thread.sleep(housekeepingIntervalMs);
      }
    } catch (InterruptedException e) {
      LOG.debug("Connection manager housekeeping interrupted, exiting");
    } catch (Exception e) {
      LOG.error("Connection manager housekeeping error", e);
    } finally {
      connectionExecutor.shutdownNow();
      openConnections.clear();
    }
  }

  private void checkOpenConnections() {
    long now = System.currentTimeMillis();

    synchronized (openConnections) {
      // Copy keys to avoid concurrent modification exception
      Set<Connection> conns = new HashSet<Connection>(openConnections.keySet());
      for (Connection conn : conns) {
        // if closed, remove from open connections
        if (conn.isClosed()) {
          openConnections.remove(conn);
          continue;
        }

        // check for connection timeouts
        ConnectionData cd = conn.getConnectionData();
        long inactivity = now - cd.getLastActivity();
        if (inactivity > warningTimeoutMs) {
          if (inactivity > (disconnectTimeoutMs + warningTimeoutMs)) {
            // this connection needs to be disconnected
            LOG.debug("checkOpenConnections():" + conn.toString() + " exceeded total timeout.");
            conn.processConnectionEvent(new ConnectionEvent(conn, ConnectionEvent.Type.CONNECTION_TIMEDOUT));
            openConnections.get(conn).cancel(true);
          } else {
            // this connection needs to be warned
            if (!cd.isWarned()) {
              LOG.debug("checkOpenConnections():" + conn.toString() + " exceeded warning timeout.");
              cd.setWarned(true);
              conn.processConnectionEvent(new ConnectionEvent(conn, ConnectionEvent.Type.CONNECTION_IDLE));
            }
          }
        }
      }
    }
  }
}
