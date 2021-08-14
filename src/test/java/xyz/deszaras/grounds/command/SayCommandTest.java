package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.SayCommand.SayMessage;
import xyz.deszaras.grounds.command.SayCommand.SayMessageEvent;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class SayCommandTest extends AbstractCommandTest {

  private static final String MESSAGE = "Hello there";

  private Place location;
  private Actor recipientActor;
  private Player recipient;
  private SayCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    location = newTestPlace("grevious's hideout");
    recipientActor = new Actor("actor2");
    recipient = newTestPlayer("recipient", Role.DENIZEN);
  }

  @Test
  public void testSuccess() throws Exception {
    setUpSuccessfulMessage();

    command = new SayCommand(actor, player, MESSAGE, false);
    assertTrue(command.execute());

    Message message = recipient.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.SAY, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));

    SayMessageEvent sayEvent =
        verifyEvent(new SayMessageEvent(player, location, null), command);
    assertTrue(((SayMessage) sayEvent.getPayload()).message.contains(MESSAGE));
  }

  @Test
  public void testSuccessOOC() throws Exception {
    setUpSuccessfulMessage();

    command = new SayCommand(actor, player, MESSAGE, true);
    assertTrue(command.execute());

    Message message = recipient.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.OOC, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));
  }

  private void setUpSuccessfulMessage() {
    player.setLocation(location);
    location.give(player);
    recipient.setLocation(location);
    location.give(recipient);
    recipient.setCurrentActor(recipientActor);

    setPlayerRoles(Role.DENIZEN);
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    command = new SayCommand(actor, player, MESSAGE, false);
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("say anything to anyone"));
  }
}
