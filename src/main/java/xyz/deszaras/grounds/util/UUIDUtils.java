package xyz.deszaras.grounds.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility methods for UUID handling.
 */
public class UUIDUtils {

  private static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  private UUIDUtils() {
  }

  /**
   * Checks if a string is a valid UUID.
   *
   * @param  s string
   * @return   true if string is a valid UUID
   */
  public static boolean isUUID(String s) {
    return UUID_PATTERN.matcher(s).matches();
  }

  /**
   * Parses a string into a UUID.
   *
   * @param  s string
   * @return   UUID
   * @throws IllegalArgumentException if the string is not a valid UUID
   */
  public static UUID getUUID(String s) {
    return UUID.fromString(s);
  }
}
