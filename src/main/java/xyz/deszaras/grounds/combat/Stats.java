package xyz.deszaras.grounds.combat;

import java.util.Map;

public interface Stats {

  Map<Skill, Integer> getSkills();
  int getRating(Skill sk);

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
}
