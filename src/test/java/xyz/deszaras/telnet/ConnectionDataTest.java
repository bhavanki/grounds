package xyz.deszaras.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.AvoidUsingHardCodedIP"})
public class ConnectionDataTest {

  private InetAddress inetAddress;
  private Socket socket;
  private ConnectionManager connectionMgr;
  private ConnectionData cd;

  @BeforeEach
  public void setUp() {
    inetAddress = mock(InetAddress.class);
    when(inetAddress.getHostName()).thenReturn("grounds.example.com");
    when(inetAddress.getHostAddress()).thenReturn("203.0.113.200");
    socket = mock(Socket.class);
    when(socket.getInetAddress()).thenReturn(inetAddress);
    when(socket.getPort()).thenReturn(1234);

    connectionMgr = mock(ConnectionManager.class);

    cd = new ConnectionData(socket, connectionMgr);
  }

  @Test
  public void testDerived() {
    assertEquals(socket, cd.getSocket());
    assertEquals(connectionMgr, cd.getManager());

    assertEquals(inetAddress, cd.getInetAddress());
    assertEquals("grounds.example.com", cd.getHostName());
    assertEquals("203.0.113.200", cd.getHostAddress());
    assertEquals(1234, cd.getPort());
  }

  @Test
  public void testDefaults() {
    assertTrue(cd.isTerminalGeometryChanged());
    int[] geom = cd.getTerminalGeometry();
    assertFalse(cd.isTerminalGeometryChanged());
    assertEquals(ConnectionData.DEFAULT_GEOMETRY[0], geom[0]);
    assertEquals(ConnectionData.DEFAULT_GEOMETRY[1], geom[1]);
    assertEquals(ConnectionData.DEFAULT_GEOMETRY[0], cd.getTerminalColumns());
    assertEquals(ConnectionData.DEFAULT_GEOMETRY[1], cd.getTerminalRows());

    assertEquals(ConnectionData.DEFAULT_TERM_TYPE, cd.getNegotiatedTerminalType());

    assertTrue(cd.getLastActivity() > 0L);
    assertFalse(cd.isWarned());
    assertFalse(cd.isLineMode());
  }

  @Test
  public void testWarningAndActivity() throws InterruptedException {
    assertFalse(cd.isWarned());
    long initialActivity = cd.getLastActivity();
    cd.setWarned(true);
    assertTrue(cd.isWarned());

    Thread.sleep(2L);
    cd.activity();

    assertFalse(cd.isWarned());
    long newActivity = cd.getLastActivity();
    assertTrue(newActivity > initialActivity);
  }

  @Test
  public void testGeometry() {
    cd.getTerminalGeometry();
    assertFalse(cd.isTerminalGeometryChanged());

    cd.setTerminalGeometry(100, 30);

    assertEquals(100, cd.getTerminalColumns());
    assertEquals(30, cd.getTerminalRows());
    assertTrue(cd.isTerminalGeometryChanged());

    int[] geom = cd.getTerminalGeometry();
    assertFalse(cd.isTerminalGeometryChanged());
    assertEquals(100, geom[0]);
    assertEquals(30, geom[1]);
  }

  @Test
  public void testTermType() {
    cd.setNegotiatedTerminalType("xterm");
    assertEquals("xterm", cd.getNegotiatedTerminalType());
  }

  @Test
  public void testLoginShell() {
    cd.setLoginShell("bash");
    assertEquals("bash", cd.getLoginShell());
  }

  @Test
  public void testLineMode() {
    cd.setLineMode(true);
    assertTrue(cd.isLineMode());
  }
}
