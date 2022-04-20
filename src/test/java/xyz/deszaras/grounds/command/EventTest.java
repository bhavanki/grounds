package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

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
    private TestEvent(Player player, Thing location, TestPayload payload) {
      super(player, location, payload);
    }
  }

  private static class PayloadLessTestEvent extends Event<Void> {
    private PayloadLessTestEvent(Player player, Thing location) {
      super(player, location, null);
    }
  }

  private TestPayload payload;
  private Place place;
  private Event event;
  private Event payloadlessEvent;

  @BeforeEach
  public void setUp() {
    place = new Place("there");
    payload = new TestPayload();
    event = new TestEvent(Player.GOD, place, payload);
    payloadlessEvent = new PayloadLessTestEvent(Player.GOD, place);
  }

  @Test
  public void testGetters() {
    assertEquals(Player.GOD, event.getPlayer());
    assertEquals(place, event.getLocation());
    assertEquals(payload, event.getPayload());

    assertNull(payloadlessEvent.getPayload());
  }

  @Test
  public void testGetAugmentedPayloadJsonString() throws Exception {
    String json = event.getAugmentedPayloadJsonString();
    verifyJson(json, Player.GOD, place, payload);
  }

  @Test
  public void testGetAugmentedPayloadJsonStringWithNoPayload() throws Exception {
    event = new TestEvent(Player.GOD, place, null);
    String json = event.getAugmentedPayloadJsonString();
    verifyJson(json, Player.GOD, place, null);

    json = payloadlessEvent.getAugmentedPayloadJsonString();
    verifyJson(json, Player.GOD, place, null);
  }

  @Test
  public void testGetAugmentedPayloadJsonStringWithNothing() throws Exception {
    event = new TestEvent(null, null, null);
    String json = event.getAugmentedPayloadJsonString();
    verifyJson(json, null, null, null);
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private void verifyJson(String json, Player player, Thing location,
                          TestPayload payload) throws Exception {
    JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
    assertTrue(jsonNode.isObject());
    ObjectNode objectNode = (ObjectNode) jsonNode;
    if (player != null) {
      assertEquals(player.getName(), objectNode.get("player").asText());
      assertEquals(player.getId().toString(), objectNode.get("playerId").asText());
    }
    if (location != null) {
      assertEquals(location.getName(), objectNode.get("location").asText());
      assertEquals(location.getId().toString(), objectNode.get("locationId").asText());
    }
    if (payload != null) {
      assertEquals(payload.theNumber, objectNode.get("theNumber").asInt());
      assertEquals(payload.theName, objectNode.get("theName").asText());
    }
  }
}
