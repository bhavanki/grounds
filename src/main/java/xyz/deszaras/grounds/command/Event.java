package xyz.deszaras.grounds.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * A record of an occurrence in the game (generated by a command that makes it
 * happen). An event usually pertains to a player, usually occurs in some place,
 * and it may have a payload, which is an arbitrary object. The object, however,
 * must be serializable to JSON.
 */
public abstract class Event<T> {

  private final Player player;
  private final Place place;
  private final T payload;

  /**
   * Creates a new event.
   *
   * @param  player  the player to whom the event pertains, if any
   * @param  place   the place where the event occurred, if any
   * @param  payload the event payload
   */
  protected Event(Player player, Place place, T payload) {
    this.player = player;
    this.place = place;
    this.payload = payload;
  }

  /**
   * Gets the player to whom the event pertains.
   *
   * @return event player
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Gets the place where the event occurred.
   *
   * @return event place
   */
  public Place getPlace() {
    return place;
  }

  /**
   * Gets the payload. This does not include any additional / overridden fields.
   *
   * @return payload
   */
  public T getPayload() {
    return payload;
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  /**
   * Gets the payload as a JSON string. Information on the event's player and
   * place are automatically inserted as "player", "playerId", "place", and
   * "placeId" fields, replacing any fields with the same name in the payload.
   *
   * @return augmented payload, as JSON string
   * @throws IllegalStateException if the payload cannot be converted to JSON
   */
  public String getAugmentedPayloadJsonString() {
    try {
      ObjectNode node = payload != null ? OBJECT_MAPPER.valueToTree(payload) :
          JsonNodeFactory.instance.objectNode();
      if (player != null) {
        node.put("player", player.getName())
            .put("playerId", player.getId().toString());
      }
      if (place != null) {
        node.put("place", place.getName())
            .put("placeId", place.getId().toString());
      }
      return OBJECT_MAPPER.writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to convert event payload to JSON", e);
    }
  }
}
