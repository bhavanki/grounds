package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class AnnounceCommandTest extends AbstractCommandTest {

  private static final String MESSAGE = "Jedi scum";

  private Place location;
  private Place location2;
  private Place location3;
  private Actor recipientActor;
  private Player recipient;
  private Actor recipientActor2;
  private Player recipient2;
  private AnnounceCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    location = newTestPlace("grevious's hideout");
    location2 = newTestPlace("outside the hideout");
    location3 = newTestPlace("in the ship");
    recipientActor = new Actor("actor2");
    recipient = newTestPlayer("recipient", Role.DENIZEN);
    recipientActor2 = new Actor("actor3");
    recipient2 = newTestPlayer("recipient2", Role.DENIZEN);
  }

  @Test
  public void testSuccess() throws Exception {
    setUpSuccessfulMessage();

    command = new AnnounceCommand(actor, player, MESSAGE);
    assertTrue(command.execute());

    Message message = recipient.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.OOC, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));

    message = recipient2.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.OOC, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));
  }

  private void setUpSuccessfulMessage() {
    player.setLocation(location);
    location.give(player);
    recipient.setLocation(location2);
    location2.give(recipient);
    recipient.setCurrentActor(recipientActor);
    recipient2.setLocation(location3);
    location3.give(recipient2);
    recipient2.setCurrentActor(recipientActor2);

    setPlayerRoles(Role.THAUMATURGE);
  }

  @Test
  public void testSuccessNoLocation() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);
    command = new AnnounceCommand(actor, player, MESSAGE);
    assertTrue(command.execute());
  }
}
