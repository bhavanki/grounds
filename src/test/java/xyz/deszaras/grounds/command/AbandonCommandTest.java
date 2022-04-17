package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class AbandonCommandTest extends AbstractCommandTest {

  private Thing thing;
  private AbandonCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    player.setCurrentActor(actor);
    setPlayerRoles(Role.DENIZEN);

    thing = newTestThing("saber");
    command = new AbandonCommand(actor, player, thing);
  }

  @Test
  public void testSuccess() throws Exception {
    thing.setOwner(player);

    assertTrue(command.execute());

    assertTrue(thing.getOwner().isEmpty());
  }

  @Test
  public void testSuccessUnowned() throws Exception {
    assertTrue(command.execute());

    assertTrue(thing.getOwner().isEmpty());

    Message message = player.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals(Message.Style.INFO, message.getStyle());
    assertEquals("No one owns that", message.getMessage());
  }

  @Test
  public void testFailureDoNotOwn() throws Exception {
    Player currentOwner = newTestPlayer("player2", Role.DENIZEN);
    thing.setOwner(currentOwner);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals(currentOwner, thing.getOwner().get());
    assertEquals("You do not own that", e.getMessage());
  }

  @Test
  public void testSuccessRelease() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);
    Player currentOwner = newTestPlayer("player2", Role.DENIZEN);
    thing.setOwner(currentOwner);

    assertTrue(command.execute());

    assertTrue(thing.getOwner().isEmpty());
  }
}
