package xyz.deszaras.grounds.command.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;

import xyz.deszaras.grounds.auth.Role;

public class DeleteMailCommandTest extends AbstractCommandTest {

  private DeleteMailCommand command;
  private Mailbox mailbox;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);
    mailbox = new Mailbox(MailCommand.getMailbox(player));
  }

  @Test
  public void testDelete() throws Exception {
    fillMailbox();

    command = new DeleteMailCommand(actor, player, 3);
    assertEquals(DeleteMailCommand.SUCCESS, command.execute());

    assertEquals(2, mailbox.size());
    assertEquals("sender3", mailbox.get(1).get().getSender());
    assertEquals("sender2", mailbox.get(2).get().getSender());
  }

  private void fillMailbox() {
    Instant ts1 = Instant.ofEpochSecond(1L);
    Missive m1 = new Missive("sender1", "subject1", List.of("player"),
                             ts1, "body1");
    testUniverse.addThing(m1.getThing());
    mailbox.deliver(m1);
    Instant ts2 = Instant.ofEpochSecond(2L);
    Missive m2 = new Missive("sender2", "subject2", List.of("player"),
                             ts2, "body2");
    testUniverse.addThing(m2.getThing());
    mailbox.deliver(m2);
    Instant ts3 = Instant.ofEpochSecond(3L);
    Missive m3 = new Missive("sender3", "subject3", List.of("player"),
                             ts3, "body3");
    testUniverse.addThing(m3.getThing());
    mailbox.deliver(m3);
  }

  @Test
  public void testDeleteEmpty() throws Exception {
    command = new DeleteMailCommand(actor, player, 1);

    assertEquals(DeleteMailCommand.NO_MESSAGES, command.execute());
  }

  @Test
  public void testGetNotEnough() throws Exception {
    fillMailbox();

    command = new DeleteMailCommand(actor, player, 4);
    String expected = String.format(DeleteMailCommand.NOT_ENOUGH_MESSAGES_FORMAT,
                                    3);
    assertEquals(expected, command.execute());
  }
}
