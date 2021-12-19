package xyz.deszaras.grounds.server;

import java.io.IOException;

/**
 * A protocol used to make connections to the server.
 */
public interface Protocol {

  String DEFAULT_HOST = "0.0.0.0"; // NOPMD

  /**
   * Starts the protocol.
   *
   * @throws IOException if the protocol fails to start
   */
  void start() throws IOException;

  /**
   * Stops the protocol.
   *
   * @throws IOException if the protocol fails to stop
   */
  void shutdown() throws IOException;
}
