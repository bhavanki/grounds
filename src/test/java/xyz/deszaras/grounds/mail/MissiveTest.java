package xyz.deszaras.grounds.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Thing;

public class MissiveTest {

  private Thing thing;
  private Missive missive;

  @Test
  public void testGetters() {
    missive = new Missive("sender", "subject", List.of("recipient"),
                          Instant.ofEpochSecond(1L), "body");

    assertEquals("sender", missive.getSender());
    assertEquals("subject", missive.getSubject());
    assertEquals(Instant.ofEpochSecond(1L), missive.getTimestamp());

    assertEquals(List.of("recipient"), missive.getRecipients());
    assertEquals("body", missive.getBody().get());
  }

  @Test
  public void testOptionals() {
    missive = new Missive("sender", "subject", List.<String>of(),
                          Instant.ofEpochSecond(1L), null);

    assertTrue(missive.getRecipients().isEmpty());
    assertTrue(missive.getBody().isEmpty());
  }

  @Test
  public void testReadFlag() {
    missive = new Missive("sender", "subject", List.of("recipient"),
                          Instant.ofEpochSecond(1L), "body");

    assertFalse(missive.isRead());
    missive.setRead(true);
    assertTrue(missive.isRead());
    missive.setRead(false);
    assertFalse(missive.isRead());
  }

  @Test
  public void testThingWrapping() {
    missive = new Missive("sender", "subject", List.<String>of(),
                          Instant.ofEpochSecond(1L), null);
    thing = missive.getThing();

    missive = Missive.of(thing);
    assertEquals(thing, missive.getThing());
    assertEquals("sender", missive.getSender());
  }
}
