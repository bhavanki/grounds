package xyz.deszaras.grounds.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for handling time data.
 */
public class TimeUtils {

  private static final String TS_SHORT_FORMAT = "yyyy-MM-dd HH:mm";
  private static final DateTimeFormatter TS_SHORT_FORMATTER =
      DateTimeFormatter.ofPattern(TS_SHORT_FORMAT);
  private static final int TS_SHORT_FORMAT_LEN = TS_SHORT_FORMAT.length();

  private static final String TS_MED_FORMAT = "MMM dd, yyyy hh:mm:ss a";
  private static final DateTimeFormatter TS_MED_FORMATTER =
      DateTimeFormatter.ofPattern(TS_MED_FORMAT);
  private static final int TS_MED_FORMAT_LEN = TS_MED_FORMAT.length();

  private TimeUtils() {
  }

  /**
   * Formats a timestamp instant as a short string, using UTC as the timezone.
   *
   * @param  t timestamp
   * @return formatted string
   */
  public static String toShortString(Instant t) {
    return toShortString(t, ZoneOffset.UTC);
  }

  /**
   * Formats a timestamp instant as a string.
   *
   * @param  t    timestamp
   * @param  zone timezone
   * @return formatted string
   */
  public static String toShortString(Instant t, ZoneId zone) {
    if (t == null) {
      return "?";
    }
    if (zone == null) {
      zone = ZoneOffset.UTC;
    }
    ZonedDateTime zdt = ZonedDateTime.ofInstant(t, zone);
    return TS_SHORT_FORMATTER.format(zdt);
  }

  /**
   * Gets the length of a timestamp instant formatted as a short string.
   *
   * @return length of a timestamp instant formatted as a short string
   */
  public static int getInstantShortStringLength() {
    return TS_SHORT_FORMAT_LEN;
  }

  /**
   * Formats a timestamp instant as a string, using UTC as the timezone.
   *
   * @param  t timestamp
   * @return formatted string
   */
  public static String toString(Instant t) {
    return toString(t, ZoneOffset.UTC);
  }

  /**
   * Formats a timestamp instant as a string.
   *
   * @param  t    timestamp
   * @param  zone timezone
   * @return formatted string
   */
  public static String toString(Instant t, ZoneId zone) {
    if (t == null) {
      return "?";
    }
    if (zone == null) {
      zone = ZoneOffset.UTC;
    }
    ZonedDateTime zdt = ZonedDateTime.ofInstant(t, zone);
    return TS_MED_FORMATTER.format(zdt);
  }

  /**
   * Gets the length of a timestamp instant formatted as a string.
   *
   * @return length of a timestamp instant formatted as a string
   */
  public static int getInstantStringLength() {
    return TS_MED_FORMAT_LEN;
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
