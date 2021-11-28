package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.net.InetAddresses;

import java.net.InetSocketAddress;

import org.apache.sshd.server.session.ServerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SshdPasswordAuthenticatorTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "test";

  private InetSocketAddress remoteAddress;
  private ServerSession session;
  private PasswordAuthenticator pa;
  private SshdPasswordAuthenticator a;

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  @BeforeEach
  public void setUp() {
    remoteAddress = new InetSocketAddress(InetAddresses.forString("192.0.2.123"), 1234);
    session = mock(ServerSession.class);
    when(session.getClientAddress()).thenReturn(remoteAddress);

    pa = mock(PasswordAuthenticator.class);
    a = new SshdPasswordAuthenticator(pa);
  }

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  @Test
  public void testAuthenticate() {
    when(pa.authenticate(USERNAME, PASSWORD, "192.0.2.123")).thenReturn(true);
    assertTrue(a.authenticate(USERNAME, PASSWORD, session));
  }

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  @Test
  public void testHandleClientPasswordChangeRequest() {
    when(pa.authenticate(USERNAME, PASSWORD, "192.0.2.123")).thenReturn(true);
    when(pa.setPassword(USERNAME, PASSWORD + "xyz")).thenReturn(true);
    assertTrue(a.handleClientPasswordChangeRequest(session, USERNAME, PASSWORD, PASSWORD + "xyz"));
  }

}
