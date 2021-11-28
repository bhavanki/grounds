package xyz.deszaras.grounds.server;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;
import xyz.deszaras.grounds.util.Argon2Utils;

/**
 * The core password authenticator implementation. Usernames and hashes are read
 * from a file; each line in the file contains a username and hash separated by
 * a colon.
 *
 * @see Argon2Utils
 */
public class PasswordAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(PasswordAuthenticator.class);

  private final ActorDatabase actorDatabase;

  /**
   * Creates a new authenticator.
   *
   * @param actorDatabase actor database containing passwords
   */
  public PasswordAuthenticator(ActorDatabase actorDatabase) {
    this.actorDatabase = Objects.requireNonNull(actorDatabase, "actorDatabase may not be null");
  }

  /*
   * Log messages here are designed to be picked up by fail2ban sshd filters.
   *
   * https://github.com/fail2ban/fail2ban/blob/master/config/filter.d/sshd.conf
   */

  private static final String ROOT_NOT_LOCALHOST_FORMAT = "Illegal user %s from %s";
  private static final String UNKNOWN_USER_FORMAT = "Invalid user %s from %s";
  private static final String LOCKED_USER_FORMAT = "User %s not allowed because account is locked";
  private static final String NO_PASSWORD_FORMAT =
      "^User not known to the underlying authentication module for %s from %s";
  private static final String AUTH_FAILURE_FORMAT = "Authentication failed for %s from %s";

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String LOCALHOST = "127.0.0.1";

  /**
   * Authenticates a user (actor) with the given password, as long as they are
   * not locked out. The root user may only be authenticated when connecting
   * from 127.0.0.1.
   *
   * @param  username    username
   * @param  password    password
   * @param  hostAddress user address
   * @return             true if authenticated, false if not
   */
  public boolean authenticate(String username, String password, String hostAddress) {

    // If actor is root, only permit connecting from 127.0.0.1.
    if (hostAddress != null &&
        username.equals(Actor.ROOT.getUsername()) && !(hostAddress.equals(LOCALHOST))) {
      LOG.warn(String.format(ROOT_NOT_LOCALHOST_FORMAT, username, hostAddress));
      return false;
    }

    Optional<ActorRecord> actorRecord = actorDatabase.getActorRecord(username);
    if (!actorRecord.isPresent()) {
      LOG.warn(String.format(UNKNOWN_USER_FORMAT, username, hostAddress));
      return false;
    }

    Instant lockedUntil = actorRecord.get().getLockedUntil();
    if (lockedUntil != null && Instant.now().isBefore(lockedUntil)) {
      LOG.warn(String.format(LOCKED_USER_FORMAT, username));
      LOG.warn("Account for " + username + " is locked until " + lockedUntil.toString());
      return false;
    }

    String hash = actorRecord.get().getPassword();
    if (hash == null) {
      LOG.warn(String.format(NO_PASSWORD_FORMAT, username, hostAddress));
      return false;
    }
    boolean result = Argon2Utils.verifyPassword(hash, password);
    if (!result) {
      LOG.warn(String.format(AUTH_FAILURE_FORMAT, username, hostAddress));
    }
    return result;
  }

  /**
   * Sets a new password.
   *
   * @param  username    username
   * @param  newPassword new password
   * @return             true if successfully changed
   */
  boolean setPassword(String username, String newPassword) {
    synchronized (actorDatabase) {
      String hash = Argon2Utils.hashPassword(newPassword);
      if (!actorDatabase.updateActorRecord(username, r -> r.setPassword(hash))) {
        return false;
      }
      try {
        actorDatabase.save();
        return true;
      } catch (IOException e) {
        LOG.error("Failed to save actor database for {} password change",
                  username, e);
        return false;
      }
    }
  }
}
