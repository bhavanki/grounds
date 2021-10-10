package xyz.deszaras.grounds.combat.grapple;

import java.util.List;

/**
 * Standard skills for the Grapple combat system.
 */
public class Skills {

  private Skills() {
  }

  public static final Skill ACCURACY =
      new Skill("Accuracy", "ac", 3, false,
                s -> new StatsDecorators.DefenseBonus(s, -1));
  public static final Skill COURAGE =
      new Skill("Courage", "co", 2, true, s -> {
        s.addAd(6);
        return s;
      });
  public static final Skill ENDURANCE =
      new Skill("Endurance", "en", 2, true,
                s -> new StatsDecorators.DefenseBonus(s, 1));
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
      new Skill("Speed", "sp", 2, true,
                s -> new StatsDecorators.ManeuverBonus(s, 1));
  public static final Skill SPIRIT =
      new Skill("Spirit", "sr", 4, true,
                s -> new StatsDecorators.StrikeBonus(s, 1));
  public static final Skill STRATEGY =
      new Skill("Strategy", "sy", 3, true,
                s -> new StatsDecorators.ApMaxSizeBonus(s, 4));
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
}
