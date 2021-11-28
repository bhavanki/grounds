package xyz.deszaras.grounds.server;

import com.google.common.net.InetAddresses;

import java.net.InetSocketAddress;

import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thin wrapper around {@link PasswordAuthenticator} for compatibility with
 * Apache SSHD.
 */
public class SshdPasswordAuthenticator
    implements org.apache.sshd.server.auth.password.PasswordAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(SshdPasswordAuthenticator.class);

  private final PasswordAuthenticator authenticator;

  /**
   * Creates a new authenticator.
   *
   * @param actorDatabase actor database containing passwords
   */
  public SshdPasswordAuthenticator(PasswordAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  public boolean authenticate(String username, String password, ServerSession session) {
    InetSocketAddress remoteAddress = (InetSocketAddress) session.getClientAddress();
    String hostAddress = InetAddresses.toAddrString(remoteAddress.getAddress());

    return authenticator.authenticate(username, password, hostAddress);
  }

  @Override
  public boolean handleClientPasswordChangeRequest(ServerSession session,
      String username, String oldPassword, String newPassword) {
    if (!authenticate(username, oldPassword, session)) {
      LOG.info("Failed to verify old password for {}", username);
      return false;
    }

    return authenticator.setPassword(username, newPassword);
  }
}
