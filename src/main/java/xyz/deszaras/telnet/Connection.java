package xyz.deszaras.telnet;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Based heavily on org.jline.builtins.telnet.ConnectionData. Its license:
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
 * Class that implements a connection with this telnet daemon. Each connection
 * is a runnable to be executed in its own thread. This might seem a waste of
 * resources, but sharing threads would require a more complex imlementation,
 * since telnet is not a stateless protocol (i.e. alive throughout a session of
 * multiple requests and responses).<p>
 *
 * A {@link ConnectionManager} is responsible for creating connections and
 * executing them.<p>
 *
 * Connection implementations must define methods for what to do with the
 * connection and for any cleanup to perform when the connection closes.
 */
public abstract class Connection implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

  private final ConnectionData connectionData;
  private final CopyOnWriteArrayList<ConnectionListener> listeners;

  private final AtomicBoolean closed;

  /**
   * Constructs a new connection.
   *
   * @param cd  connection data
   */
  public Connection(ConnectionData cd) {
    connectionData = cd;
    listeners = new CopyOnWriteArrayList<ConnectionListener>();

    closed = new AtomicBoolean(false);
  }

  /**
   * Gets the connection data for this connection.
   *
   * @return connection data
   */
  public ConnectionData getConnectionData() {
    return connectionData;
  }

  /**
   * Runs this connection.
   *
   * @see #doRun()
   */
  public void run() {
    try {
      doRun();
    } catch (InterruptedException e) {
      LOG.debug("Connection running interrupted, closing");
    } catch (Exception ex) {
      LOG.error("Error running connection, closing", ex);
    } finally {
      close();
    }
  }

  /**
   * Performs the work needed for this connection. For example, an
   * implementation may spawn a shell for the connected user to interact with.
   *
   * @throws Exception if an unrecoverable error occurs and the connection
   * should close
   */
  protected abstract void doRun() throws Exception;

  /**
   * Performs any cleanup work needed when this connection closes. The default
   * implementation does nothing.
   *
   * @throws Exception if there is a problem performing cleanup work
   */
  protected void cleanup() throws Exception {
  }

  /**
   * Closes the connection and its underlying socket. If the connection is
   * alread closed, does nothing.
   */
  private void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }

    try {
      cleanup();
    } catch (Exception ex) {
      LOG.error("Error cleaning up connection", ex);
    }
    try {
      connectionData.getSocket().close();
    } catch (IOException ex) {
      LOG.warn("Failed to close connection socket", ex);
    }
  }

  /**
   * Returns if this connection is closed.
   *
   * @return true if connection is closed
   */
  public boolean isClosed() {
    return closed.get();
  }

  /**
   * Registers a listener with this connection.
   *
   * @param cl listener to be registered
   */
  public void addConnectionListener(ConnectionListener cl) {
    listeners.add(cl);
  }

  /**
   * Deregisters a listener with this connection.
   *
   * @param cl listener to be deregistered
   */
  public void removeConnectionListener(ConnectionListener cl) {
    listeners.remove(cl);
  }

  /**
   * Synchronously sends the given event to all listeners.
   *
   * @param ce event
   */
  public void processConnectionEvent(ConnectionEvent ce) {
    for (ConnectionListener cl : listeners) {
      switch (ce.getType()) {
        case CONNECTION_IDLE:
          cl.connectionIdle(ce);
          break;
        case CONNECTION_TIMEDOUT:
          cl.connectionTimedOut(ce);
          break;
        case CONNECTION_LOGOUTREQUEST:
          cl.connectionLogoutRequest(ce);
          break;
        case CONNECTION_BREAK:
          cl.connectionSentBreak(ce);
          break;
        case CONNECTION_TERMINAL_GEOMETRY_CHANGED:
          cl.connectionTerminalGeometryChanged(ce);
          break;
        default:
          throw new InternalError("Unsupported event type " + ce.getType());
      }
    }
  }
}
