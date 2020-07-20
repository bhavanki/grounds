package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;

/**
 * A thing that houses extensions to the game.
 */
public class Extension extends Thing {

  /**
   * Only certain roles may work with extensions.
   */
  public static final Set<Role> PERMITTED_ROLES = Set.of(Role.BARD, Role.THAUMATURGE);

  public Extension(String name) {
    super(name);
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

  public static Extension build(String name, List<String> buildArgs) {
    Extension extension = new Extension(name);
    return extension;
  }
}
