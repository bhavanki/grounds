package xyz.deszaras.grounds.model;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * The names of specially handled attributes.
 */
public final class AttrNames {

  private AttrNames() {
  }

  /**
   * The attribute for the name of a thing.
   */
  public static final String NAME = "name";
  /**
   * The attribute for the description of a thing.
   */
  public static final String DESCRIPTION = "description";
  /**
   * The attribute for the universe of a thing.
   */
  public static final String UNIVERSE = "universe";
  /**
   * The attribute for the location of a thing.
   */
  public static final String LOCATION = "location";
  /**
   * The attribute for the owner of a thing.
   */
  public static final String OWNER = "owner";

  /**
   * The names of all specially handled attributes as a set.
   */
  public static final Set<String> ALL_NAMES = ImmutableSet.<String>builder()
      .add(NAME)
      .add(DESCRIPTION)
      .add(UNIVERSE)
      .add(LOCATION)
      .add(OWNER)
      .build();
}
