package xyz.deszaras.grounds.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class MailboxTest {

  private Universe universe;
  private Thing thing;
  private Mailbox mailbox;

  @BeforeEach
  public void setUp() {
    universe = new Universe("test");
    Universe.setCurrent(universe);
    thing = new Thing("mailbox");
    universe.addThing(thing);
    mailbox = new Mailbox(thing);
  }

  @Test
  public void testDeliverAndGetOne() {
    Missive m1 = getTestMissive("subject1", Instant.ofEpochSecond(1L));
    assertTrue(mailbox.deliver(m1));
    assertEquals(1, mailbox.size());
    assertEquals(m1.getThing(), mailbox.get(1).get().getThing());
  }

  @Test
  public void testDeliverAndGetMultiple() {
    Missive m1 = getTestMissive("subject1", Instant.ofEpochSecond(1L));
    assertTrue(mailbox.deliver(m1));
    Missive m2 = getTestMissive("subject2", Instant.ofEpochSecond(2L));
    assertTrue(mailbox.deliver(m2));

    assertEquals(2, mailbox.size());
    assertEquals(m2.getThing(), mailbox.get(1).get().getThing());
    assertEquals(m1.getThing(), mailbox.get(2).get().getThing());
  }

  @Test
  public void testGetAllInReverseChronoOrder() {
    Missive m1 = getTestMissive("subject1", Instant.ofEpochSecond(1L));
    assertTrue(mailbox.deliver(m1));
    Missive m2 = getTestMissive("subject2", Instant.ofEpochSecond(2L));
    assertTrue(mailbox.deliver(m2));

    assertEquals(2, mailbox.size());
    List<Missive> contents = mailbox.getAllInReverseChronoOrder();
    assertEquals(2, contents.size());
    assertEquals(m2.getThing(), contents.get(0).getThing());
    assertEquals(m1.getThing(), contents.get(1).getThing());
  }

  @Test
  public void testEmptyMailbox() {
    assertEquals(0, mailbox.size());
    assertEquals(0, mailbox.getAllInReverseChronoOrder().size());

    assertTrue(mailbox.get(1).isEmpty());
  }

  @Test
  public void testDelete() {
    Missive m1 = getTestMissive("subject1", Instant.ofEpochSecond(1L));
    assertTrue(mailbox.deliver(m1));
    Missive m2 = getTestMissive("subject2", Instant.ofEpochSecond(2L));
    assertTrue(mailbox.deliver(m2));

    assertTrue(mailbox.delete(m1));
    assertEquals(1, mailbox.size());
    assertEquals(m2.getThing(), mailbox.get(1).get().getThing());
  }

  private Missive getTestMissive(String subject, Instant timestamp) {
    Missive m = new Missive("sender", subject, List.of("recipient"),
                            timestamp, "body");
    universe.addThing(m.getThing());
    return m;
  }
}
