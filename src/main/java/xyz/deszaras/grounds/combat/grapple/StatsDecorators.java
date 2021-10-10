package xyz.deszaras.grounds.combat.grapple;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;

/**
 * Available stats decorators in the Grapple combat system. All of these
 * decorators must accept zero or more integer constructor arguments.
 */
public class StatsDecorators {

  private StatsDecorators() {
  }

  /**
   * A decorator that alters all skill ratings.
   */
  static class SkillRatingBonus extends StatsDecorator {
    private final int delta;
    SkillRatingBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
    }
    @VisibleForTesting
    int getDelta() {
      return delta;
    }
    @Override
    public int getRating(Skill sk) {
      return delegate.getRating(sk) + delta;
    }
    @Override
    public boolean isLegal() {
      if (!super.isLegal()) {
        return false;
      }
      for (Skill sk : delegate.getSkills().keySet()) {
        if (getRating(sk) < Limits.MIN_SKILL_RATING ||
            getRating(sk) > Limits.MAX_SKILL_RATING) {
          return false;
        }
      }
      return true;
    }
    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(delta);
    }
  }

  /**
   * A decorator that alters the AP max size.
   */
  static class ApMaxSizeBonus extends StatsDecorator {
    private final int delta;
    ApMaxSizeBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
    }
    @VisibleForTesting
    int getDelta() {
      return delta;
    }
    @Override
    public int getApMaxSize() {
      return delegate.getApMaxSize() + delta;
    }
    @Override
    public boolean isLegal() {
      if (!super.isLegal()) {
        return false;
      }
      return getApMaxSize() >= Limits.MIN_AP_MAX_SIZE &&
          getApMaxSize() <= Limits.MAX_AP_MAX_SIZE;
    }
    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(delta);
    }
  }

  /**
   * A decorator that alters defense.
   */
  static class DefenseBonus extends StatsDecorator {
    private final int delta;
    DefenseBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
    }
    @VisibleForTesting
    int getDelta() {
      return delta;
    }
    @Override
    public int getDefense() {
      return delegate.getDefense() + delta;
    }
    @Override
    public boolean isLegal() {
      if (!super.isLegal()) {
        return false;
      }
      return getDefense() >= Limits.MIN_DEFENSE &&
          getDefense() <= Limits.MAX_DEFENSE;
    }
    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(delta);
    }
  }

  /**
   * A decorator that alters AD available for maneuvers.
   */
  static class ManeuverBonus extends StatsDecorator {
    private final int delta;
    ManeuverBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
    }
    @VisibleForTesting
    int getDelta() {
      return delta;
    }
    @Override
    public int getManeuverBonus() {
      return delegate.getManeuverBonus() + delta;
    }
    @Override
    public boolean isLegal() {
      if (!super.isLegal()) {
        return false;
      }
      return getManeuverBonus() >= Limits.MIN_MANEUVER_BONUS &&
          getManeuverBonus() <= Limits.MAX_MANEUVER_BONUS;
    }
    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(delta);
    }
  }

  /**
   * A decorator that alters SD.
   */
  static class StrikeBonus extends StatsDecorator {
    private final int delta;
    StrikeBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
    }
    @VisibleForTesting
    int getDelta() {
      return delta;
    }
    @Override
    public int getStrikeBonus() {
      return delegate.getStrikeBonus() + delta;
    }
    @Override
    public boolean isLegal() {
      if (!super.isLegal()) {
        return false;
      }
      return getStrikeBonus() >= Limits.MIN_STRIKE_BONUS &&
          getStrikeBonus() <= Limits.MAX_STRIKE_BONUS;
    }
    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(delta);
    }
  }

  /**
   * Builds a stats decorator wrapping the given delegate. The simple class name
   * of the decorator is specified by the {@code name} argument, and the integer
   * {@code buildArgs} are passed to the decorator constructor.
   *
   * @param  delegate  stats to wrap
   * @param  name      simple class name of decorator
   * @param  buildArgs integers to pass to decorator constructor
   * @return           new decorator
   * @throws IllegalArgumentException if the name is unrecognized or the wrong
   *                                  number of integer build arguments is
   *                                  passed
   */
  public static StatsDecorator build(Stats delegate, String name,
                                     List<Integer> buildArgs) {
    switch (name) {
      case "SkillRatingBonus":
        checkArgument(buildArgs.size() == 1,
                      "SkillRatingBonus takes one integer argument");
        return new SkillRatingBonus(delegate, buildArgs.get(0));
      case "ApMaxSizeBonus":
        checkArgument(buildArgs.size() == 1,
                      "ApMaxSizeBonus takes one integer argument");
        return new ApMaxSizeBonus(delegate, buildArgs.get(0));
      case "DefenseBonus":
        checkArgument(buildArgs.size() == 1,
                      "DefenseBonus takes one integer argument");
        return new DefenseBonus(delegate, buildArgs.get(0));
      case "ManeuverBonus":
        checkArgument(buildArgs.size() == 1,
                      "ManeuverBonus takes one integer argument");
        return new ManeuverBonus(delegate, buildArgs.get(0));
      case "StrikeBonus":
        checkArgument(buildArgs.size() == 1,
                      "StrikeBonus takes one integer argument");
        return new StrikeBonus(delegate, buildArgs.get(0));
      default:
        throw new IllegalArgumentException("Unknown decorator " + name);
    }
  }
}
