package xyz.deszaras.grounds.auth;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

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

  /**
   * The set of all roles.
   */
  public static final Set<Role> ALL_ROLES =
      ImmutableSet.copyOf(Arrays.asList(Role.values()));

  /**
   * The set of all non-guest roles. These are players of the game.
   */
  public static final Set<Role> NON_GUEST_ROLES =
      ImmutableSet.of(DENIZEN, OWNER, BARD, ADEPT, THAUMATURGE);

  /**
   * The set of all wizard roles.
   */
  public static final Set<Role> WIZARD_ROLES =
      ImmutableSet.of(BARD, ADEPT, THAUMATURGE);

  /**
   * Checks if the player has a wizard role in their current universe.
   *
   * @param player player
   * @return true if player is a wizard in their current universe, or is GOD
   */
  public static boolean isWizard(Player player) {
    return isWizard(player, player.getUniverse());
  }

  /**
   * Checks if the player has a wizard role in a universe.
   *
   * @param player player
   * @param universe universe
   * @return true if player is a wizard in the given universe, or is GOD
   */
  public static boolean isWizard(Player player, Universe universe) {
    if (player.equals(Player.GOD)) {
      return true;
    }
    Set<Role> roles = universe.getRoles(player);
    return roles.stream().anyMatch(r -> Role.WIZARD_ROLES.contains(r));
  }
}
