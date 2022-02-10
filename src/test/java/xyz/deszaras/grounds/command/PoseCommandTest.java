package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

public class PoseCommandTest extends AbstractCommandTest {

  private static final String MESSAGE = "swings a lightsaber";

  private Place location;
  private Actor recipient1Actor;
  private Player recipient1;
  private Actor recipient2Actor;
  private Player recipient2;
  private PoseCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    location = newTestPlace("grevious's hideout");
    recipient1Actor = new Actor("r1actor");
    recipient1 = newTestPlayer("recipient1", Role.DENIZEN);
    recipient2Actor = new Actor("r2actor");
    recipient2 = newTestPlayer("recipient2", Role.DENIZEN);
  }

  @Test
  public void testSuccess() throws Exception {
    setUpSuccessfulMessage();

    command = new PoseCommand(actor, player, MESSAGE);
    assertTrue(command.execute());

    Message message = recipient1.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.POSE, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));

    message = recipient2.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.POSE, message.getStyle());
    assertTrue(message.getMessage().contains(MESSAGE));
  }

  private void setUpSuccessfulMessage() {
    player.setLocation(location);
    location.give(player);
    recipient1.setLocation(location);
    location.give(recipient1);
    recipient1.setCurrentActor(recipient1Actor);
    recipient2.setLocation(location);
    location.give(recipient2);
    recipient2.setCurrentActor(recipient2Actor);

    setPlayerRoles(Role.DENIZEN);
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    command = new PoseCommand(actor, player, MESSAGE);
    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("pose to anyone"));
  }
}
