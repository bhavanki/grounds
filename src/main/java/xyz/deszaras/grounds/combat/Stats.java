package xyz.deszaras.grounds.combat;

import java.util.Map;

public interface Stats {

  Map<Skill, Integer> getSkills();
  int getRating(Skill sk);

  Map<Skill, Boolean> getSkillUses();
  void useSkill(Skill sk);
  boolean isUsed(Skill sk);
  boolean allSkillsUsed();
  void resetSkillUses();

  int getApMaxSize();
  int getAd();
  int addAd(int amt);
  int setAd(int amt);

  int getSd();
  int addSd(int amt);
  int setSd(int amt);

  int getDefense();

  int getMaxWounds();
  int getWounds();
  int wound();
  int wound(int amt);
  boolean isOut();

  int getManeuverBonus();
  int getStrikeBonus();

  /**
   * Checks whether these stats are legal according to combat rules, e.g., that
   * all stats are within required limits.
   *
   * @return          true if these stats are legal
   */
  boolean isLegal();
}
