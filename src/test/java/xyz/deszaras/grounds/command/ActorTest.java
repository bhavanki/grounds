package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class ActorTest {

  private Actor actor;

  @BeforeEach
  public void setUp() {
    actor = new Actor("bob");
  }

  @Test
  public void testGettersAndSetters() {
    assertEquals("bob", actor.getUsername());

    Player player = new Player("robobob", Universe.VOID);
    actor.setCurrentPlayer(player);
    assertEquals(player, actor.getCurrentPlayer());

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    InetAddress actorAddress = InetAddresses.forString("1.2.3.4");
    actor.setMostRecentIPAddress(actorAddress);
    assertEquals(actorAddress, actor.getMostRecentIPAddress());
  }

  @Test
  public void testSimpleMessaging() throws Exception {
    Message message = new Message(Player.GOD, Message.Style.INFO, "Hello");
    actor.sendMessage(message);
    assertEquals(message, actor.getNextMessage());
  }
}
