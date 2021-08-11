package xyz.deszaras.grounds.command.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;
import xyz.deszaras.grounds.model.Player;

import xyz.deszaras.grounds.auth.Role;

public class GetMailCommandTest extends AbstractCommandTest {

  private GetMailCommand command;
  private Mailbox mailbox;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);
    mailbox = new Mailbox(MailCommand.getMailbox(player));
  }

  @Test
  public void testGet() throws Exception {
    fillMailbox();

    command = new GetMailCommand(actor, player, 3);
    String listing = command.execute();

    Pattern p1 = Pattern.compile("^.*From:.*sender1$", Pattern.MULTILINE);
    assertTrue(p1.matcher(listing).find());
    Pattern p2 = Pattern.compile("^.*Sent:.*$", Pattern.MULTILINE);
    assertTrue(p2.matcher(listing).find());
    Pattern p3 = Pattern.compile("^.*Subject.*subject1$", Pattern.MULTILINE);
    assertTrue(p3.matcher(listing).find());
    Pattern p4 = Pattern.compile("^body1$", Pattern.MULTILINE);
    assertTrue(p4.matcher(listing).find());
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
  public void testGetEmpty() throws Exception {
    command = new GetMailCommand(actor, player, 1);

    assertEquals(GetMailCommand.NO_MESSAGES, command.execute());
  }

  @Test
  public void testGetNotEnough() throws Exception {
    fillMailbox();

    command = new GetMailCommand(actor, player, 4);
    String expected = String.format(GetMailCommand.NOT_ENOUGH_MESSAGES_FORMAT,
                                    3);
    assertEquals(expected, command.execute());
  }
}
