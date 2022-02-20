package xyz.deszaras.grounds.api;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.api.method.ApiMethodFactory;
import xyz.deszaras.grounds.command.CommandExecutor;

/**
 * A server for receiving API calls from plugins.
 */
public class ApiServer implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ApiServer.class);

  private final Path socketPath;
  private final ExecutorService serverExecutor;
  private final ExecutorService handlerExecutor;
  private final PluginCallTracker pluginCallTracker;
  private final ApiMethodFactory apiMethodFactory;
  private final CountDownLatch shutdownLatch;

  private Future<?> serverFuture;
  private ServerSocketChannel serverChannel;

  /**
   * Creates a new API server.
   *
   * @param  socketPath path to domain socket file
   */
  public ApiServer(Path socketPath) {
    this.socketPath = Objects.requireNonNull(socketPath);

    serverExecutor =
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                          .setDaemon(false)
                                          .setNameFormat("api-server")
                                          .build());
    handlerExecutor =
        Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                      .setDaemon(false)
                                      .setNameFormat("api-handler-%d")
                                      .build());
    pluginCallTracker = new PluginCallTracker();
    apiMethodFactory = new ApiMethodFactory();

    shutdownLatch = new CountDownLatch(1);
  }

  /**
   * Starts this API server.
   *
   * @throws IOException if the domain socket cannot be opened or bound
   */
  public void start() throws IOException {
    UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);
    serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
    serverChannel.bind(socketAddress);
    LOG.info("API server bound to domain socket {}", socketPath);

    serverFuture = serverExecutor.submit(this);
  }

  /**
   * Shuts down this API server. This also deletes the domain socket file.
   *
   * @throws IOException          if the server channel cannot be closed
   * @throws InterruptedException if shutdown is interrupted while waiting
   *                              for the server to exit
   */
  public void shutdown() throws IOException, InterruptedException {
    serverFuture.cancel(true);

    shutdownLatch.await();
    handlerExecutor.shutdownNow();
    serverExecutor.shutdown();
    serverChannel.close();
    Files.delete(socketPath);
  }

  @Override
  public void run() {
    CommandExecutor commandExecutor = CommandExecutor.getInstance();
    while (true) {
      try {
        SocketChannel clientChannel = serverChannel.accept();
        handlerExecutor.submit(new ApiHandler(clientChannel, pluginCallTracker,
                                              apiMethodFactory, commandExecutor));
      } catch (ClosedByInterruptException e) {
        break;
      } catch (IOException e) {
        // oh noes
        break;
      }
    }
    shutdownLatch.countDown();
  }

  /**
   * Gets the plugin call tracker for this API server.
   *
   * @return plugin call tracker
   */
  public PluginCallTracker getPluginCallTracker() {
    return pluginCallTracker;
  }
}
