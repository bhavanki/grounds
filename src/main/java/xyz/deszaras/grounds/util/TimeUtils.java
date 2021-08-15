package xyz.deszaras.grounds.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
// import java.time.format.FormatStyle;

/**
 * Utility methods for handling time data.
 */
public class TimeUtils {

  private static final String TS_SHORT_FORMAT = "yyyy-MM-dd HH:mm";
  private static final DateTimeFormatter TS_SHORT_FORMATTER =
      DateTimeFormatter.ofPattern(TS_SHORT_FORMAT);
  private static final int TS_SHORT_FORMAT_LEN = TS_SHORT_FORMAT.length();

  private TimeUtils() {
  }

  public static String toShortString(Instant timestamp) {
    return toShortString(timestamp, ZoneOffset.UTC);
  }

  public static String toShortString(Instant timestamp, ZoneId zone) {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(timestamp, zone);
    return TS_SHORT_FORMATTER.format(zdt);
  }

  public static int getInstantShortStringLength() {
    return TS_SHORT_FORMAT_LEN;
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
