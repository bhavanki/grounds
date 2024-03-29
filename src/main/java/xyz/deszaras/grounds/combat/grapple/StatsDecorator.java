package xyz.deszaras.grounds.combat.grapple;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A decorator around a {@link Stats} object (which may be yet another
 * decorator) which alters stats. The default method implementations in this
 * abstract base class pass through stats unchanged to the delegate. Subclasses
 * should override at least one of the methods to have an effect.
 */
public abstract class StatsDecorator implements Stats {

  protected Stats delegate;

  /**
   * Creates a new decorator.
   *
   * @param  delegate stats to decorate
   */
  protected StatsDecorator(Stats delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  /**
   * Gets the stats this decorator wraps.
   *
   * @return decorated stats
   */
  Stats getDelegate() {
    return delegate;
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

  @Override
  public boolean isLegal() {
    return delegate.isLegal();
  }

  public ProtoModel.StatsDecorator toDecoratorProto() {
    return ProtoModel.StatsDecorator.newBuilder()
        .setName(this.getClass().getSimpleName())
        .addAllBuildArgs(this.getBuildIntList())
        .build();
  }

  @Override
  public ProtoModel.Stats toProto() {
    ProtoModel.Stats delegateProto = delegate.toProto();
    return ProtoModel.Stats.newBuilder()
        .setBaseStats(delegateProto.getBaseStats())
        .addAllStatsDecorators(delegateProto.getStatsDecoratorsList())
        .addStatsDecorators(toDecoratorProto())
        .build();
  }

  /**
   * Gets the integer arguments to pass to
   * {@link StatsDecorators#build(Stats, String, List)} to construct a new
   * equivalent instance of this decorator. The default implementation returns
   * an empty list.
   *
   * @return list of integer build arguments
   */
  protected List<Integer> getBuildIntList() {
    return List.of();
  }
}
