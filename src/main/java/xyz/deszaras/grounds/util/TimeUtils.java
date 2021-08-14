package xyz.deszaras.grounds.util;

import java.time.Duration;

/**
 * Utility methods for handling time data.
 */
public class TimeUtils {

  private TimeUtils() {
  }

  /**
   * Formats a duration as a string.
   *
   * @param d duration
   * @return formatted string
   */
  public static String toString(Duration d) {
    if (d == null) {
      return "?";
    }
    return String.format("%dh%dm%ds", d.toHours(), d.toMinutesPart(),
                         d.toSecondsPart());
  }
}
