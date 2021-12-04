package xyz.deszaras.telnet;

import java.util.Objects;

/*
 * Based heavily on org.jline.builtins.telnet.ConnectionEvent. Its license:
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
 ***/

/**
 * Events sent to connection listeners.
 */
public class ConnectionEvent {

  private final Connection source;
  private final Type type;

  /**
   * Constructs a new event.
   *
   * @param source relevant connection for event
   * @param type   event type
   */
  public ConnectionEvent(Connection source, Type type) {
    this.source = Objects.requireNonNull(source);
    this.type = Objects.requireNonNull(type);
  }

  /**
   * Gets the source.
   *
   * @return source
   */
  public Connection getSource() {
    return source;
  }

  /**
   * Gets the type.
   *
   * @return type
   */
  public Type getType() {
      return type;
  }

  /**
   * Valid event types.
   */
  public enum Type {
    /**
     * Indicates that a connection has been idle long enough to be warned.
     */
    CONNECTION_IDLE,
    /**
     * Indicates that a connection has timed out and should be disconnected.
     */
    CONNECTION_TIMEDOUT,
    /**
     * Indicates that non-graceful connection logout has been requested via
     * Control-D.
     */
    CONNECTION_LOGOUTREQUEST,
    /**
     * Indicates that the connection sent an NVT BREAK.
     */
    CONNECTION_BREAK,
    /**
     * Indicates that the connection sent a NAWS (size change).
     */
    CONNECTION_TERMINAL_GEOMETRY_CHANGED;
  }
}
