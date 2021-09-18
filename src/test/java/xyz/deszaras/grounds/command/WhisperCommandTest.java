package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class WhisperCommandTest extends AbstractCommandTest {

  private static final String MESSAGE = "I like you";

  private Place location;
  private Actor recipientActor;
  private Player recipient;
  private WhisperCommand command;

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

    command = new WhisperCommand(actor, player, recipient, MESSAGE);
    assertTrue(command.execute());

    Message message = recipient.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.WHISPER, message.getStyle());
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
}
