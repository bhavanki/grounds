package xyz.deszaras.grounds.command.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;

import xyz.deszaras.grounds.auth.Role;

public class MarkReadMailCommandTest extends AbstractCommandTest {

  private MarkReadMailCommand command;
  private Mailbox mailbox;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);
    mailbox = new Mailbox(MailCommand.getMailbox(player));
  }

  @Test
  public void testMarkRead() throws Exception {
    fillMailbox();

    for (int i = 1; i <= 3; i++) {
      assertFalse(mailbox.get(i).get().isRead());
    }
    Missive m3 = mailbox.get(3).get();

    command = new MarkReadMailCommand(actor, player, 3, true);
    assertEquals(MarkReadMailCommand.MARKED_READ, command.execute());
    assertTrue(m3.isRead());

    command = new MarkReadMailCommand(actor, player, 3, false);
    assertEquals(MarkReadMailCommand.MARKED_UNREAD, command.execute());
    assertFalse(m3.isRead());
  }

  private void fillMailbox() {
    Instant ts1 = Instant.ofEpochSecond(1L);
    Missive m1 = new Missive("sender1", "subject1", List.of("player1", "player2"),
                             ts1, "body1");
    testUniverse.addThing(m1.getThing());
    mailbox.deliver(m1);
    Instant ts2 = Instant.ofEpochSecond(2L);
    Missive m2 = new Missive("sender2", "subject2", List.of("player1", "player2"),
                             ts2, "body2");
    testUniverse.addThing(m2.getThing());
    mailbox.deliver(m2);
    Instant ts3 = Instant.ofEpochSecond(3L);
    Missive m3 = new Missive("sender3", "subject3", List.of("player1", "player2"),
                             ts3, "body3");
    testUniverse.addThing(m3.getThing());
    mailbox.deliver(m3);
  }

  @Test
  public void testGetEmpty() throws Exception {
    command = new MarkReadMailCommand(actor, player, 1, true);

    assertEquals(MarkReadMailCommand.NO_MESSAGES, command.execute());
  }

  @Test
  public void testGetNotEnough() throws Exception {
    fillMailbox();

    command = new MarkReadMailCommand(actor, player, 4, true);
    String expected = String.format(MarkReadMailCommand.NOT_ENOUGH_MESSAGES_FORMAT,
                                    3);
    assertEquals(expected, command.execute());
  }
}
