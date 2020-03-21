package xyz.deszaras.grounds.auth;

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
   * A non-wizard, typical player role in a universe.
   */
  DENIZEN,
  /**
   * A guest in a universe.
   */
  GUEST
}
