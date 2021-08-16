package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

public class TimeUtilsTest {

  @Test
  public void testInstantToShortString() {
    Instant ts = ZonedDateTime.of(2021, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)
        .toInstant();
    assertEquals("2021-01-02 03:04", TimeUtils.toShortString(ts));

    assertEquals("?", TimeUtils.toShortString((Instant) null));
  }

  @Test
  public void testInstantToString() {
    Instant ts = ZonedDateTime.of(2021, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)
        .toInstant();
    assertEquals("Jan 02, 2021 03:04:05 AM", TimeUtils.toString(ts));

    assertEquals("?", TimeUtils.toString((Instant) null));
  }

  @Test
  public void testDurationToString() {
    Duration d = Duration.ofSeconds(3661);
    assertEquals("1h1m1s", TimeUtils.toString(d));

    assertEquals("?", TimeUtils.toString((Duration) null));
  }
}
