package xyz.deszaras.grounds.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import de.mkammerer.argon2.Argon2Helper;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for working with Argon2.
 */
public class Argon2Utils {

  // https://www.twelve21.io/how-to-use-argon2-for-password-hashing-in-java/

  private static final Argon2 ARGON2;
  private static final int ARGON2_MEMORY_KB = 1024 * 256;
  private static final int ARGON2_PARALLELISM = 2;

  static {
    ARGON2 = Argon2Factory.create(Argon2Types.ARGON2id);
  }

  private Argon2Utils() {
  }

  /**
   * Verifies a password given its expected hash.
   *
   * @param  hash     password hash
   * @param  password password
   * @return          true if password is verified
   */
  public static boolean verifyPassword(String hash, String password) {
    synchronized (ARGON2) {
      return ARGON2.verify(hash, password, StandardCharsets.UTF_8);
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
