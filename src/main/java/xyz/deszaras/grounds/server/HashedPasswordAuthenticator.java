package xyz.deszaras.grounds.server;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import de.mkammerer.argon2.Argon2Helper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

/**
 * A password authenticator that uses Argon2. Usernames and hashes are
 * read from a file; each line in the file contains a username and hash
 * separated by a colon.
 */
public class HashedPasswordAuthenticator implements PasswordAuthenticator {

  // https://www.twelve21.io/how-to-use-argon2-for-password-hashing-in-java/

  private static final Logger LOG = LoggerFactory.getLogger(HashedPasswordAuthenticator.class);

  private static final Argon2 ARGON2;
  private static final int ARGON2_MEMORY_KB = 1024 * 256;
  private static final int ARGON2_PARALLELISM = 2;

  static {
    ARGON2 = Argon2Factory.create(Argon2Types.ARGON2id);
  }

  private final ActorDatabase actorDatabase;

  /**
   * Creates a new authenticator.
   *
   * @param actorDatabase actor database containing passwords
   */
  public HashedPasswordAuthenticator(ActorDatabase actorDatabase) {
    this.actorDatabase = Objects.requireNonNull(actorDatabase, "actorDatabase may not be null");
  }

  @Override
  public boolean authenticate(String username, String password, ServerSession session) {
    Optional<ActorRecord> actorRecord = actorDatabase.getActorRecord(username);
    if (!actorRecord.isPresent()) {
      LOG.info("Actor {} unknown", username);
      return false;
    }
    String hash = actorRecord.get().getPassword();
    if (hash == null) {
      LOG.info("No password set for actor {}", username);
      return false;
    }
    synchronized (ARGON2) {
      return ARGON2.verify(hash, password, StandardCharsets.UTF_8);
    }
  }

  @Override
  public boolean handleClientPasswordChangeRequest(ServerSession session,
      String username, String oldPassword, String newPassword) {
    synchronized (actorDatabase) {
      if (!authenticate(username, oldPassword, session)) {
        LOG.info("Failed to verify old password for {}", username);
        return false;
      }
      String hash = hashPassword(newPassword);
      return actorDatabase.updateActorRecord(username, r -> r.setPassword(hash));
    }
  }

  /**
   * Generates a hash for the given password.
   *
   * @param password password to hash
   * @return hash
   */
  public static String hashPassword(String password) {
    synchronized (ARGON2) {
      int iterations = Argon2Helper.findIterations(ARGON2, 500, 65536, 1);
      return ARGON2.hash(iterations, ARGON2_MEMORY_KB, ARGON2_PARALLELISM,
                         password, StandardCharsets.UTF_8);
    }
  }
}
