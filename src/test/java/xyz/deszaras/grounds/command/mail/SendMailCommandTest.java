package xyz.deszaras.grounds.command.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;
import xyz.deszaras.grounds.model.Player;

import xyz.deszaras.grounds.auth.Role;

public class SendMailCommandTest extends AbstractCommandTest {

  private SendMailCommand command;
  private Player recipient1;
  private Player recipient2;
  private Player recipient3;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    recipient1 = new Player("recipient1");
    testUniverse.addThing(recipient1);
    recipient2 = new Player("recipient2");
    testUniverse.addThing(recipient2);
    recipient3 = new Player("recipient3");
    testUniverse.addThing(recipient3);
  }

  @Test
  public void testSend() throws Exception {
    String subject = "subject1";
    String body = "body1";

    command = new SendMailCommand(actor, player,
                                  Set.of(recipient1, recipient2),
                                  subject, body);
    assertTrue(command.execute());

    Mailbox mailbox1 = new Mailbox(MailCommand.getMailbox(recipient1));
    assertEquals(1, mailbox1.size());
    Missive missive1 = mailbox1.get(1).get();
    assertEquals("player", missive1.getSender());
    assertEquals("subject1", missive1.getSubject());
    assertEquals("body1", missive1.getBody().get());
    List<String> recipientNames1 = missive1.getRecipients();
    assertEquals(2, recipientNames1.size());
    assertTrue(recipientNames1.contains("recipient1"));
    assertTrue(recipientNames1.contains("recipient2"));

    Mailbox mailbox2 = new Mailbox(MailCommand.getMailbox(recipient2));
    assertEquals(1, mailbox2.size());
    Missive missive2 = mailbox2.get(1).get();
    assertEquals("player", missive2.getSender());
    assertEquals("subject1", missive2.getSubject());
    assertEquals("body1", missive2.getBody().get());
    List<String> recipientNames2 = missive1.getRecipients();
    assertEquals(2, recipientNames2.size());
    assertTrue(recipientNames2.contains("recipient1"));
    assertTrue(recipientNames2.contains("recipient2"));

    Mailbox mailbox3 = new Mailbox(MailCommand.getMailbox(recipient3));
    assertEquals(0, mailbox3.size());
  }
}
