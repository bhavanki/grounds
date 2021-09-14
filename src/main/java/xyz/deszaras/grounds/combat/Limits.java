package xyz.deszaras.grounds.combat;

/**
 * Stat and bonus limits to be enforced.
 */
public class Limits {

  public static final int MIN_SKILL_RATING = 1;
  public static final int MAX_SKILL_RATING = 6;

  public static final int MIN_AP_MAX_SIZE = 6;
  public static final int MAX_AP_MAX_SIZE = 20;

  public static final int MIN_DEFENSE = 1;
  public static final int MAX_DEFENSE = 6;

  public static final int MIN_MAX_WOUNDS = 1;
  public static final int MAX_MAX_WOUNDS = 5;

  public static final int MIN_MANEUVER_BONUS = -1;
  public static final int MAX_MANEUVER_BONUS = 1;

  public static final int MIN_STRIKE_BONUS = -1;
  public static final int MAX_STRIKE_BONUS = 1;

  private Limits() {
  }
}
