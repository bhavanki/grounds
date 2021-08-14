package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class TimeUtilsTest {

  @Test
  public void testDurationToString() {
    Duration d = Duration.ofSeconds(3661);
    assertEquals("1h1m1s", TimeUtils.toString(d));

    assertEquals("?", TimeUtils.toString((Duration) null));
  }
}
