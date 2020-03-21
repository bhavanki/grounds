package xyz.deszaras.grounds.server;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import de.mkammerer.argon2.Argon2Helper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final Path passwordFile;
  private final Map<String, String> passwords;

  /**
   * Creates a new authenticator.
   *
   * @param passwordFile path to password file
   * @throws IOException if the password file cannot be read
   */
  public HashedPasswordAuthenticator(Path passwordFile) throws IOException {
    this.passwordFile = Objects.requireNonNull(passwordFile, "passwordFile may not be null");
    passwords = loadPasswords(passwordFile);

  }

  private static final Map<String, String> loadPasswords(Path passwordFile) throws IOException {
    return Files.lines(passwordFile, StandardCharsets.UTF_8)
        .map(l -> l.trim())
        .filter(l -> !l.isEmpty())
        .filter(l -> !l.startsWith("#"))
        .collect(Collectors.toMap(s -> s.substring(0, s.indexOf(":")),
                                  s -> s.substring(s.indexOf(":") + 1)));
  }

  private static final void savePasswords(Map<String, String> passwords, Path passwordFile) throws IOException {
    Files.write(passwordFile,
                passwords.entrySet().stream()
                    .map(e -> String.format("%s:%s", e.getKey(), e.getValue()))
                    .sorted()
                    .collect(Collectors.toList()),
                StandardCharsets.UTF_8);
  }

  @Override
  public synchronized boolean authenticate(String username, String password,
      ServerSession session) {
    if (!passwords.containsKey(username)) {
      LOG.info("No password stored for {}", username);
      return false;
    }
    String hash = passwords.get(username);
    synchronized (ARGON2) {
      return ARGON2.verify(hash, password, StandardCharsets.UTF_8);
    }
  }

  @Override
  public synchronized boolean handleClientPasswordChangeRequest(ServerSession session,
      String username, String oldPassword, String newPassword) {
    if (!authenticate(username, oldPassword, session)) {
      LOG.info("Failed to verify old password for {}", username);
      return false;
    }
    String hash = hashPassword(newPassword);
    passwords.put(username, hash);
    try {
      savePasswords(passwords, passwordFile);
    } catch (IOException e) {
      LOG.error("Failed to save passwords", e);
      return false;
    }
    return true;
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
