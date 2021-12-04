package xyz.deszaras.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ConnectionTest {

  private static class TestConnection extends Connection {

    private boolean ran = false;
    private boolean cleanedUp = false;

    private TestConnection(ConnectionData cd) {
      super(cd);
    }

    @Override
    public void doRun() {
      ran = true;
    }

    @Override
    public void cleanup() {
      cleanedUp = true;
    }

    private boolean ran() {
      return ran;
    }

    private boolean cleanedUp() {
      return cleanedUp;
    }
  }

  private Socket socket;
  private ConnectionData cd;
  private Connection conn;

  @BeforeEach
  public void setUp() {
    socket = mock(Socket.class);
    cd = mock(ConnectionData.class);
    when(cd.getSocket()).thenReturn(socket);
    conn = new TestConnection(cd);
  }

  @Test
  public void testDefaults() {
    assertEquals(cd, conn.getConnectionData());
    assertFalse(conn.isClosed());
  }

  @Test
  public void testRun() throws Exception {
    conn.run();

    assertTrue(((TestConnection) conn).ran());
    assertTrue(conn.isClosed());
    assertTrue(((TestConnection) conn).cleanedUp());
    verify(socket).close();
  }

  @Test
  public void testInterruption() throws Exception {
    CountDownLatch interrupted = new CountDownLatch(1);
    conn = new Connection(cd) {
      @Override
      public void doRun() {
        try {
          Thread.sleep(60000L);
        } catch (InterruptedException e) {
          interrupted.countDown();
        }
      }
    };

    Thread ct = new Thread(conn);
    ct.start();
    ct.interrupt();
    ct.join();

    interrupted.await();
    assertTrue(conn.isClosed());
    verify(socket).close();
  }

  @Test
  public void testException() throws Exception {
    conn = new Connection(cd) {
      @Override
      public void doRun() {
        throw new IllegalStateException("oopsie");
      }
    };

    conn.run();

    assertTrue(conn.isClosed());
    verify(socket).close();
  }

  private static final class TestConnectionListener implements ConnectionListener {
    boolean idle;
    boolean timedout;
    boolean logout;
    boolean sentbreak;
    boolean geomchanged;

    @Override
    public void connectionIdle(ConnectionEvent ce) {
      idle = true;
    }
    @Override
    public void connectionTimedOut(ConnectionEvent ce) {
      timedout = true;
    }
    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {
      logout = true;
    }
    @Override
    public void connectionSentBreak(ConnectionEvent ce) {
      sentbreak = true;
    }
    @Override
    public void connectionTerminalGeometryChanged(ConnectionEvent ce) {
      geomchanged = true;
    }
  }

  @Test
  public void testEvents() {
    TestConnectionListener l1 = new TestConnectionListener();
    TestConnectionListener l2 = new TestConnectionListener();
    conn.addConnectionListener(l1);
    conn.addConnectionListener(l2);

    for (ConnectionEvent.Type eventType : ConnectionEvent.Type.values()) {
      conn.processConnectionEvent(new ConnectionEvent(conn, eventType));
    }

    assertTrue(l1.idle);
    assertTrue(l1.timedout);
    assertTrue(l1.logout);
    assertTrue(l1.sentbreak);
    assertTrue(l1.geomchanged);

    assertTrue(l2.idle);
    assertTrue(l2.timedout);
    assertTrue(l2.logout);
    assertTrue(l2.sentbreak);
    assertTrue(l2.geomchanged);
  }
}
