package xyz.deszaras.grounds.combat.grapple;

import java.util.List;

/**
 * Standard skills for the Grapple combat system.
 */
public class Skills {

  private Skills() {
  }

  public static final Skill ACCURACY =
      new Skill("Accuracy", "ac", 3, false, s -> new DefenseBonus(s, -1));
  public static final Skill COURAGE =
      new Skill("Courage", "co", 2, true, s -> {
        s.addAd(6);
        return s;
      });
  public static final Skill ENDURANCE =
      new Skill("Endurance", "en", 2, true, s -> new DefenseBonus(s, 1));
  public static final Skill INTIMIDATION =
      new Skill("Intimidation", "in", 2, false, s -> {
        s.addAd(-4);
        return s;
      });
  public static final Skill LEADERSHIP =
      new Skill("Leadership", "ld", 2, false, s -> {
        s.addAd(3);
        return s;
      });
  public static final Skill MEDICAL =
      new Skill("Medical", "md", 4, false, s -> {
        s.wound(-1);
        return s;
      });
  public static final Skill SPEED =
      new Skill("Speed", "sp", 2, true, s -> new ManeuverBonus(s, 1));
  public static final Skill SPIRIT =
      new Skill("Spirit", "sr", 4, true, s -> new StrikeBonus(s, 1));
  public static final Skill STRATEGY =
      new Skill("Strategy", "sy", 3, true, s -> new ApMaxSizeBonus(s, 4));
  public static final Skill TACTICS =
      new Skill("Tactics", "tc", 2, false, s -> {
        s.addSd(2);
        return s;
      });
  public static final Skill TAUNTING =
      new Skill("Taunting", "tt", 2, false, s -> {
        s.addSd(-2);
        return s;
      });
  public static final Skill TRICKSTER =
      new Skill("Trickster", "tr", 4, false, s -> {
        s.setSd(0);
        return s;
      });

  private static final List<Skill> ALL_SKILLS = List.of(
    ACCURACY,
    COURAGE,
    ENDURANCE,
    INTIMIDATION,
    LEADERSHIP,
    MEDICAL,
    SPEED,
    SPIRIT,
    STRATEGY,
    TACTICS,
    TAUNTING,
    TRICKSTER
  );

  /**
   * Gets the skill with the given name or abbreviation.
   *
   * @param  name skill name
   * @return      skill
   * @throws IllegalArgumentException if no skill has the given name
   */
  public static Skill forName(String name) {
    for (Skill sk : ALL_SKILLS) {
      if (sk.getName().equalsIgnoreCase(name)) {
        return sk;
      }
      if (sk.getAbbrev().equalsIgnoreCase(name)) {
        return sk;
      }
    }
    throw new IllegalArgumentException("No skill has name " + name);
  }

  private static class SkillRatingBonus extends StatsDecorator {
    private final int delta;
    public SkillRatingBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
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
  }

  private static class ApMaxSizeBonus extends StatsDecorator {
    private final int delta;
    public ApMaxSizeBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
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
  }

  private static class DefenseBonus extends StatsDecorator {
    private final int delta;
    public DefenseBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
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
  }

  private static class ManeuverBonus extends StatsDecorator {
    private final int delta;
    public ManeuverBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
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
  }

  private static class StrikeBonus extends StatsDecorator {
    private final int delta;
    public StrikeBonus(Stats delegate, int delta) {
      super(delegate);
      this.delta = delta;
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
  }
}
