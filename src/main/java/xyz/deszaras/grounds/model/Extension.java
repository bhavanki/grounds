package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Policy;

/**
 * A thing that houses extensions to the game.
 */
public class Extension extends Thing {

  public Extension(String name, Universe universe) {
    super(name, universe);
  }

  /**
   * Creates a new extension.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Extension(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);
  }

  public static Extension build(String name, Universe universe, List<String> buildArgs) {
    Extension extension = new Extension(name, universe);
    return extension;
  }
}
