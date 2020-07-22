package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class EventTest {

  private static class TestPayload {
    public final int theNumber;
    public final String theName;

    private TestPayload() {
      theNumber = 42;
      theName = "bob";
    }
  }

  private static class TestEvent extends Event<TestPayload> {
    private TestEvent(Player player, TestPayload payload) {
      super(player, payload);
    }
  }

  private TestPayload payload;
  private Event event;

  @BeforeEach
  public void setUp() {
    payload = new TestPayload();
    event = new TestEvent(Player.GOD, payload);
  }

  @Test
  public void testGetters() {
    assertEquals(Player.GOD, event.getPlayer());
    assertEquals(payload, event.getPayload());
  }

  @Test
  public void testGetPayloadJsonString() {
    String json = event.getPayloadJsonString();
    assertNotNull(json);
  }

  @Test
  public void testGetPayloadJsonStringWithNoPayload() {
    event = new TestEvent(Player.GOD, null);
    String json = event.getPayloadJsonString();
    assertNull(json);
  }
}
