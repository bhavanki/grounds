package xyz.deszaras.telnet;

/*
 * Based heavily on org.jline.builtins.telnet.PortListener. Its license:
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener for incoming telnet connections.
 */
public class PortListener implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(PortListener.class);

  private final String name;
  private final AtomicBoolean available;

  private Thread thread;
  private ServerSocket serverSocket;
  private ConnectionManager connectionManager;

  /**
   * Constructs a new port listener.
   *
   * @param name          listener name
   * @param ip            IP address to bind to; null for all local addresses
   * @param port          listener port
   * @param backlogLength server socket backlog length
   * @throws IOException if a server socket cannot be established
   */
  public PortListener(String name, String ip, int port, int backlogLength)
      throws IOException {
    this.name = Objects.requireNonNull(name);
    available = new AtomicBoolean(false);

    // TODO port range check
    // TODO backlogLength range check
    serverSocket = new ServerSocket(port, backlogLength,
                                    ip != null ? InetAddress.getByName(ip) : null);
    LOG.info("Listening on port {} with backlog length {}", port, backlogLength);
  }

  /**
   * Gets the name of this listener.
   *
   * @return listener name
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if this listener is available.
   *
   * @return true if available, false otherwise
   */
  public boolean isAvailable() {
    return available.get();
  }

  /**
   * Sets whether this listener is available. Normally a listener is available
   * as long as it is accepting socket connections, but the flag can be used to
   * explicitly reject connections without stopping the listener.
   *
   * @param available availability flag
   */
  public void setAvailable(boolean available) {
    this.available.set(available);
  }

  /**
   * Gets the connection manager handling connections from this listener.
   *
   * @return connection manager
   */
  public ConnectionManager getConnectionManager() {
    return connectionManager;
  }

  /**
   * Sets the connection manager handling connections from this listener. Be
   * sure to set this before starting the listener.
   *
   * @param connectionManager connection manager
   */
  public void setConnectionManager(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /**
   * Starts this listener.
   *
   * @throws IllegalStateException if the connection manager is not set
   */
  public synchronized void start() {
    if (connectionManager == null) {
      throw new IllegalStateException("connection manager not set");
    }
    // TODO use executor instead
    thread = new Thread(this);
    thread.start();
    available.set(true);
  }

  /**
   * Stops this listener.
   */
  public synchronized void stop() {
    if (thread != null) {
      available.set(false);
      try {
        serverSocket.close();
      } catch (IOException e) {
        LOG.error("Failed to close server socket", e);
      }
      // thread.interrupt();
      thread = null;
    }
  }

  /**
   * Listens forever for incoming connections on a server socket and hands them
   * off to the connection manager.
   */
  public void run() {
    try {
      while (true) {
        Socket s = null;
        try {
          s = serverSocket.accept();
          if (available.get()) {
            connectionManager.startConnection(s);
          } else {
            // just shut down the socket
            LOG.debug("New connections unavailable, closing socket");
            try {
              s.close();
            } catch (IOException e) {
              LOG.info("Failed to close socket when unavailable", e);
            }
          }
        } catch (IllegalStateException e) {
          LOG.info("Failed to establish new connection", e);
          try {
            if (s != null) {
              s.close();
            }
          } catch (IOException e2) {
            LOG.info("Failed to close socket to rejected connection", e2);
          }
        } catch (SocketException e) {
          LOG.info("Server socket closed, shutting down");
          LOG.debug("Accept exception", e);
          break;
        }
      }
    } catch (IOException e) {
      LOG.error("Exception listening for connections", e);
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException ex) {
          LOG.warn("Failed to close server socket", ex);
        }
      }
    }
  }
}
