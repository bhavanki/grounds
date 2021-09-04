package xyz.deszaras.grounds.combat;

import java.util.List;

/**
 * Standard skills for the Grapple combat system.
 */
public class Skills {

  private Skills() {
  }

  public static final Skill ACCURACY =
      new Skill("Accuracy", 3, false, s -> new DefenseBonus(s, -1));
  public static final Skill COURAGE =
      new Skill("Courage", 2, true, s -> {
        s.addAd(6);
        return s;
      });
  public static final Skill ENDURANCE =
      new Skill("Endurance", 2, true, s -> new DefenseBonus(s, 1));
  public static final Skill INTIMIDATION =
      new Skill("Intimidation", 2, false, s -> {
        s.addAd(-4);
        return s;
      });
  public static final Skill LEADERSHIP =
      new Skill("Leadership", 2, false, s -> {
        s.addAd(3);
        return s;
      });
  public static final Skill MEDICAL =
      new Skill("Medical", 4, false, s -> {
        s.wound(-1);
        return s;
      });
  public static final Skill SPEED =
      new Skill("Speed", 2, true, s -> new ManeuverBonus(s, 1));
  public static final Skill SPIRIT =
      new Skill("Spirit", 4, true, s -> new StrikeBonus(s, 1));
  public static final Skill STRATEGY =
      new Skill("Strategy", 3, true, s -> new ApMaxSizeBonus(s, 4));
  public static final Skill TACTICS =
      new Skill("Tactics", 2, false, s -> {
        s.addSd(2);
        return s;
      });
  public static final Skill TAUNTING =
      new Skill("Taunting", 2, false, s -> {
        s.addSd(-2);
        return s;
      });
  public static final Skill TRICKSTER =
      new Skill("Trickster", 4, false, s -> {
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
   * Gets the skill with the given name.
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
  }
}
