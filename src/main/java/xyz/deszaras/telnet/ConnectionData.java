package xyz.deszaras.telnet;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
 * A utility class for assorted connection data.
 */
public class ConnectionData {

  static final int[] DEFAULT_GEOMETRY = { 80, 25 };
  static final String DEFAULT_TERM_TYPE = "default";

  private final Socket socket;
  private final ConnectionManager connectionManager;

  private final InetAddress address;
  private final String hostName;
  private final String hostAddress;
  private final int port;

  private final int[] terminalGeometry;
  private String negotiatedTerminalType;
  private boolean terminalGeometryChanged;

  private long lastActivity;
  private boolean warned;

  private final Map<String, String> environment;
  private String loginShell;
  private boolean lineMode;

  /**
   * Constructs a new instance, deriving data from the given socket.
   *
   * @param sock socket of the inbound connection
   * @param cm   connection manager
   */
  public ConnectionData(Socket sock, ConnectionManager cm) {
    socket = sock;
    connectionManager = cm;

    address = sock.getInetAddress();
    hostName = address.getHostName();
    hostAddress = address.getHostAddress();
    port = sock.getPort();

    terminalGeometry = new int[2];
    terminalGeometry[0] = DEFAULT_GEOMETRY[0];  // width
    terminalGeometry[1] = DEFAULT_GEOMETRY[1];  // height
    negotiatedTerminalType = DEFAULT_TERM_TYPE;
    terminalGeometryChanged = true;

    environment = new HashMap<String, String>(20);
    lineMode = false;

    //this will stamp the first activity for validity :)
    activity();
  }

  /**
   * Gets the connection manager.
   *
   * @return connection manager
   */
  public ConnectionManager getManager() {
    return connectionManager;
  }

  /**
   * Gets the socket for this connection.
   *
   * @return socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Gets the socket address for this connection.
   *
   * @return socket address
   */
  public InetAddress getInetAddress() {
    return address;
  }

  /**
   * Returns the host name for this connection, as resolved from its socket
   * address upon construction of this object.
   *
   * @return socket host name
   */
  public String getHostName() {
      return hostName;
  }

  /**
   * Gets the string form of the socket address for this connection.
   *
   * @return socket host address as a string
   */
  public String getHostAddress() {
    return hostAddress;
  }

  /**
   * Gets the port for this connection.
   *
   * @return port
   */
  public int getPort() {
    return port;
  }

  /**
   * Gets the time of the last activity on this connection.
   *
   * @return last activity time, in milliseconds since the epoch
   */
  public long getLastActivity() {
    return lastActivity;
  }

  /**
   * Updates the time of the last activity on this connection. This also clears
   * the idle warning flag.
   */
  public void activity() {
    warned = false;
    lastActivity = System.currentTimeMillis();
  }

  /**
   * Returns whether the idle warning flag has been set for this connection.
   *
   * @return idle warning flag
   */
  public boolean isWarned() {
    return warned;
  }

  /**
   * Sets the idle warning flag for this connection. If set to false, then the
   * time of the last activity on this connection is also updated.
   *
   * @param bool idle warning flag
   */
  public void setWarned(boolean bool) {
    warned = bool;
    if (!bool) {
      lastActivity = System.currentTimeMillis();
    }
  }

  /**
   * Sets new terminal geometry data, setting the change flag for it.
   *
   * @param width  new terminal width (columns)
   * @param height new terminal height (rows)
   */
  void setTerminalGeometry(int width, int height) {
    terminalGeometry[0] = width;
    terminalGeometry[1] = height;
    terminalGeometryChanged = true;
  }

  /**
   * Gets the terminal geometry, resetting the change flag for it. The two
   * elements of the returned array are the width in columns and height in rows,
   * respectively.
   *
   * @return terminal geometry: width and height
   */
  public int[] getTerminalGeometry() {
    terminalGeometryChanged = false;
    return Arrays.copyOf(terminalGeometry, terminalGeometry.length);
  }

  /**
   * Gets the width of the terminal geometry; this does not alter the change
   * flag for it.
   *
   * @return width (number of columns)
   */
  public int getTerminalColumns() {
    return terminalGeometry[0];
  }

  /**
   * Gets the height of the terminal geometry; this does not alter the change
   * flag for it.
   *
   * @return height (number of rows)
   */
  public int getTerminalRows() {
    return terminalGeometry[1];
  }

  /**
   * Checks if the terminal geometry has changed.
   *
   * @return true if it has changed, false otherwise
   */
  public boolean isTerminalGeometryChanged() {
    return terminalGeometryChanged;
  }

  /**
   * Gets the terminal type that has been negotiated between the client and the
   * server.
   *
   * @return negotiated terminal type
   */
  public String getNegotiatedTerminalType() {
    return negotiatedTerminalType;
  }

  /**
   * Sets the terminal type that has been negotiated between the client and the
   * server.
   *
   * @param termtype negotiated terminal type
   */
  void setNegotiatedTerminalType(String termtype) {
    negotiatedTerminalType = termtype;
  }

  /**
   * Gets the environment (variables to be passed between shells). This is not
   * a defensive copy, so changes made to the returned map affect this object.
   *
   * @return environment as map of environment variable keys and values
   */
  public Map<String, String> getEnvironment() {
      return environment;
  }

  /**
   * Gets the login shell name.
   *
   * @return login shell
   */
  public String getLoginShell() {
    return loginShell;
  }

  /**
   * Sets the login shell name.
   *
   * @param s login shell
   */
  public void setLoginShell(String s) {
    loginShell = s;
  }

  /**
   * Gets the line mode flag for this connection.
   *
   * @return line mode flag
   */
  public boolean isLineMode() {
    return lineMode;
  }

  /**
   * Sets the line mode flag for this connection. Set this prior to
   * initialization.
   *
   * @param lm line mode flag
   */
  public void setLineMode(boolean b) {
    lineMode = b;
  }
}

