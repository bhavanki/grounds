package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Argon2UtilsTest {

  @Test
  public void testHashing() {
    String hash = Argon2Utils.hashPassword("grounds");
    assertTrue(Argon2Utils.verifyPassword(hash, "grounds"));
    assertFalse(Argon2Utils.verifyPassword(hash, "grounds1"));
  }
}
