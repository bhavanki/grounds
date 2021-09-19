package xyz.deszaras.grounds.combat.grapple;

import java.util.Map;

/**
 * Combat statistics in the Grapple combat system.
 */
public interface Stats {

  /**
   * Gets skills and their ratings.
   *
   * @return map of skills and associated ratings
   */
  Map<Skill, Integer> getSkills();
  /**
   * Gets the rating for the given skill.
   *
   * @param  sk skill
   * @return    skill rating
   */
  int getRating(Skill sk);

  /**
   * Gets skill uses. A true value indicates that a skill has been used.
   *
   * @return map of skills and associated uses
   */
  Map<Skill, Boolean> getSkillUses();
  /**
   * Marks a skill as used.
   *
   * @param sk skill
   */
  void useSkill(Skill sk);
  /**
   * Determines if a skill has been used.
   *
   * @param  sk skill
   * @return    true if skill has been used
   */
  boolean isUsed(Skill sk);
  /**
   * Determines if all skills have been used.
   *
   * @return true if all skills have been used
   */
  boolean allSkillsUsed();
  /**
   * Resets all skill uses to false.
   */
  void resetSkillUses();

  /**
   * Gets the AP max size.
   *
   * @return AP max size
   */
  int getApMaxSize();
  /**
   * Gets the current number of action dice (AD).
   *
   * @return current number of AD
   */
  int getAd();
  /**
   * Adds an amount of action dice (AD); may be negative. The final number of
   * AD may not exceed the AP max size.
   *
   * @param  amt AD to add
   * @return     new number of AD
   */
  int addAd(int amt);
  /**
   * Sets the amount of action dice (AD). The final number of AD may not exceed
   * the AP max size.
   *
   * @param  amt AD to set
   * @return     new number of AD
   */
  int setAd(int amt);

  /**
   * Gets the current number of strike dice (SD).
   *
   * @return current number of SD
   */
  int getSd();
  /**
   * Adds an amount of strike dice (SD); may be negative.
   *
   * @param  amt SD to add
   * @return     new number of SD
   */
  int addSd(int amt);
  /**
   * Sets the ammount of strike dice (SD).
   *
   * @param  amt SD to set
   * @return     new number of SD
   */
  int setSd(int amt);

  /**
   * Gets the defense.
   *
   * @return [description]
   */
  int getDefense();

  /**
   * Gets the maximum number of wounds.
   *
   * @return max wounds
   */
  int getMaxWounds();
  /**
   * Gets the current number of wonds.
   *
   * @return current number of wounds
   */
  int getWounds();
  /**
   * Adds one wound.
   *
   * @return new number of wounds
   */
  int wound();
  /**
   * Adds an amount of wounds; may be negative. The final number of wounds may
   * not exceed the max wounds.
   *
   * @param  amt wounds to add
   * @return     new number of wounds
   */
  int wound(int amt);
  /**
   * Determines if the player with these stats is knocked out, i.e., has at
   * least as many wounds as max wounds.
   *
   * @return true if stats indicate player is knocked out
   */
  boolean isOut();

  /**
   * Gets the maneuver bonus; may be negative.
   *
   * @return maneuver bonus
   */
  int getManeuverBonus();
  /**
   * Gets the strike bonus; may be negative.
   *
   * @return strike bonus
   */
  int getStrikeBonus();

  /**
   * Checks whether these stats are legal according to combat rules, e.g., that
   * all stats are within required limits.
   *
   * @return          true if these stats are legal
   */
  boolean isLegal();
}
