package xyz.deszaras.grounds.auth;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;

/**
 * A role is used to determine a player's permissions in a universe.
 */
public enum Role {
  /**
   * The most powerful wizard role in a universe.
   */
  THAUMATURGE,
  /**
   * A moderately powerful player role in a universe.
   */
  ADEPT,
  /**
   * The least powerful wizard role in a universe.
   */
  BARD,
  /**
   * An owner of a thing. This role only works in the context of the thing
   * that is owned, and is not a "universal" role.
   */
  OWNER,
  /**
   * A non-wizard, typical player role in a universe.
   */
  DENIZEN,
  /**
   * A guest in a universe.
   */
  GUEST;

  public static final Set<Role> ALL_ROLES =
      ImmutableSet.copyOf(Arrays.asList(Role.values()));

  public static final Set<Role> NON_GUEST_ROLES =
      ImmutableSet.of(DENIZEN, OWNER, BARD, ADEPT, THAUMATURGE);

  public static final Set<Role> WIZARD_ROLES =
      ImmutableSet.of(BARD, ADEPT, THAUMATURGE);
}
