package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class PageCommandTest extends AbstractCommandTest {

  private static final String MESSAGE = "Well hello there";

  private Place location;
  private Place location2;
  private Actor recipientActor;
  private Player recipient;
  private PageCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    location = newTestPlace("grevious's hideout");
    location2 = newTestPlace("outside the hideout");
    recipientActor = new Actor("actor2");
    recipient = newTestPlayer("recipient", Role.DENIZEN);
  }

  @Test
  public void testSuccess() throws Exception {
    setUpSuccessfulMessage();

    command = new PageCommand(actor, player, recipient, MESSAGE);
    assertTrue(command.execute());

    Message message = recipient.getNextMessage();
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

    setPlayerRoles(Role.DENIZEN);
  }

  @Test
  public void testSuccessNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    command = new PageCommand(actor, player, recipient, MESSAGE);
    assertTrue(command.execute());
  }
}
