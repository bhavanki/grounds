package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class ClaimCommandTest extends AbstractCommandTest {

  private Thing thing;
  private ClaimCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    player.setCurrentActor(actor);
    setPlayerRoles(Role.DENIZEN);

    thing = newTestThing("saber");
    command = new ClaimCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    assertTrue(command.execute());

    assertEquals(player, thing.getOwner().get());
  }

  @Test
  public void testSuccessAlreadyOwn() throws Exception {
    thing.setOwner(player);

    assertTrue(command.execute());

    assertEquals(player, thing.getOwner().get());

    Message message = player.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.INFO, message.getStyle());
    assertEquals("You already own that", message.getMessage());
  }

  @Test
  public void testFailureAlreadyOwned() throws Exception {
    Player currentOwner = newTestPlayer("player2", Role.DENIZEN);
    thing.setOwner(currentOwner);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals(currentOwner, thing.getOwner().get());
    assertEquals("That is already owned by someone else",
                 e.getMessage());
  }

  @Test
  public void testSuccessSeize() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);
    Player currentOwner = newTestPlayer("player2", Role.DENIZEN);
    thing.setOwner(currentOwner);

    assertTrue(command.execute());

    assertEquals(player, thing.getOwner().get());
  }
}
