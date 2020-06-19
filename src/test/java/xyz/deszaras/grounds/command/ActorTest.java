package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class ActorTest {

  private Actor actor;

  @BeforeEach
  public void setUp() {
    actor = new Actor("bob");
  }

  @Test
  public void testGettersAndSetters() {
    assertEquals("bob", actor.getUsername());

    Player player = new Player("robobob");
    actor.setCurrentPlayer(player);
    assertEquals(player, actor.getCurrentPlayer());

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    InetAddress actorAddress = InetAddresses.forString("1.2.3.4");
    actor.setMostRecentIPAddress(actorAddress);
    assertEquals(actorAddress, actor.getMostRecentIPAddress());

    assertTrue(actor.getPreferences().isEmpty());
    actor.setPreference("this", "that");
    assertEquals("that", actor.getPreference("this").get());
    assertEquals(1, actor.getPreferences().size());
    assertEquals("that", actor.getPreferences().get("this"));
    assertTrue(actor.getPreference("missing").isEmpty());

    actor.setPreferences(ImmutableMap.of("hi", "there"));
    assertEquals("there", actor.getPreference("hi").get());
    assertTrue(actor.getPreference("this").isEmpty());
    actor.setPreference("hi", null);
    assertTrue(actor.getPreference("hi").isEmpty());
  }

  @Test
  public void testSimpleMessaging() throws Exception {
    Message message = new Message(Player.GOD, Message.Style.INFO, "Hello");
    actor.sendMessage(message);
    assertEquals(message, actor.getNextMessage());
  }
}
