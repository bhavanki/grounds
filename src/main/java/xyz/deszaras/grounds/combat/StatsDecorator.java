package xyz.deszaras.grounds.combat;

import java.util.Map;
import java.util.Objects;

public abstract class StatsDecorator implements Stats {

  protected Stats delegate;

  protected StatsDecorator(Stats delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  @Override
  public Map<Skill, Integer> getSkills() {
    return delegate.getSkills();
  }
  @Override
  public int getRating(Skill sk) {
    return delegate.getRating(sk);
  }

  @Override
  public Map<Skill, Boolean> getSkillUses() {
    return delegate.getSkillUses();
  }
  @Override
  public void useSkill(Skill sk) {
    delegate.useSkill(sk);
  }
  @Override
  public boolean isUsed(Skill sk) {
    return delegate.isUsed(sk);
  }
  @Override
  public boolean allSkillsUsed() {
    return delegate.allSkillsUsed();
  }
  @Override
  public void resetSkillUses() {
    delegate.resetSkillUses();
  }

  @Override
  public int getApMaxSize() {
    return delegate.getApMaxSize();
  }
  @Override
  public int getAd() {
    return delegate.getAd();
  }
  @Override
  public int addAd(int amt) {
    return delegate.addAd(amt);
  }
  @Override
  public int setAd(int amt) {
    return delegate.setAd(amt);
  }

  @Override
  public int getSd() {
    return delegate.getSd();
  }
  @Override
  public int addSd(int amt) {
    return delegate.addSd(amt);
  }
  @Override
  public int setSd(int amt) {
    return delegate.setSd(amt);
  }

  @Override
  public int getDefense() {
    return delegate.getDefense();
  }

  @Override
  public int getMaxWounds() {
    return delegate.getMaxWounds();
  }
  @Override
  public int getWounds() {
    return delegate.getWounds();
  }
  @Override
  public int wound() {
    return delegate.wound();
  }
  @Override
  public int wound(int amt) {
    return delegate.wound(amt);
  }
  @Override
  public boolean isOut() {
    return delegate.isOut();
  }

  @Override
  public int getManeuverBonus() {
    return delegate.getManeuverBonus();
  }
  @Override
  public int getStrikeBonus() {
    return delegate.getStrikeBonus();
  }
}
