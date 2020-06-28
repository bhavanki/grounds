package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Message;

public class PlayerTest {

  private Actor actor;
  private Player player;

  @BeforeEach
  public void setUp() {
    actor = new Actor("bob0");
    player = new Player("bob");
    player.setCurrentActor(actor);
  }

  @Test
  public void testSimpleMessaging() throws Exception {
    Message message = new Message(Player.GOD, Message.Style.INFO, "Hello");
    player.sendMessage(message);
    assertEquals(message, player.getNextMessage());
  }

  @Test
  public void testMessageWithNoActor() {
    Message message = new Message(Player.GOD, Message.Style.INFO, "Hello");
    player.setCurrentActor(null);
    player.sendMessage(message);
    assertNull(player.peekNextMessage());
  }

  @Test
  public void testMuting() {
    Player sender = new Player("muted");
    player.setMuteList(List.of(sender));
    Message message = new Message(sender, Message.Style.INFO, "Hello");
    player.sendMessage(message);
    assertNull(player.peekNextMessage());
  }
}
