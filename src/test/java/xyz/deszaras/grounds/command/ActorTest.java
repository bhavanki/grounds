package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ActorTest {

  private Actor actor;

  @BeforeEach
  public void setUp() {
    actor = new Actor("bob");
  }

  @Test
  public void testGettersAndSetters() {
    assertEquals("bob", actor.getUsername());

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    InetAddress actorAddress = InetAddresses.forString("1.2.3.4");
    actor.setMostRecentIPAddress(actorAddress);
    assertEquals(actorAddress, actor.getMostRecentIPAddress());

    Instant now = Instant.now();
    actor.setLastLoginTime(now);
    assertEquals(now, actor.getLastLoginTime());

    assertTrue(actor.getPreferences().isEmpty());
    actor.setPreference("this", "that");
    assertEquals("that", actor.getPreference("this").get());
    assertEquals(1, actor.getPreferences().size());
    assertEquals("that", actor.getPreferences().get("this"));
    assertTrue(actor.getPreference("missing").isEmpty());

    actor.setPreferences(ImmutableMap.of("hi", "there"));
    assertEquals("there", actor.getPreference("hi").get());
    assertTrue(actor.getPreference("this").isEmpty());
    actor.setPreference("hi", null);
    assertTrue(actor.getPreference("hi").isEmpty());
  }

  @Test
  public void testTimezone() {
    assertEquals(ZoneOffset.UTC, actor.getTimezone());

    actor.setPreference(Actor.PREFERENCE_TIMEZONE, "America/New_York");
    assertEquals(ZoneId.of("America/New_York"), actor.getTimezone());

    actor.setPreference(Actor.PREFERENCE_TIMEZONE, "My area");
    assertEquals(ZoneOffset.UTC, actor.getTimezone());
  }
}
