package xyz.deszaras.grounds.combat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The rules for the Grapple combat system.
 */
public class Rules {

  public interface Input {
  }

  public interface Output {
    String formatResult();
  }

  public static class ManeuverInput implements Input {
    public final Stats stats;
    public final Skill skill;
    public final int ad;

    public ManeuverInput(Stats stats, Skill skill, int ad) {
      this.stats = Objects.requireNonNull(stats);
      this.skill = Objects.requireNonNull(skill);
      Preconditions.checkArgument(ad >= 1 && ad <= 3,
                                  "ad must be between 1 and 3");
      Preconditions.checkArgument(stats.getAd() >= ad,
                                  "Not enough action dice");
      this.ad = ad;
    }
  }

  public static class ManeuverOutput implements Output {
    public final boolean success;
    public final int numSuccs;
    public final int numRolls;
    public final int adSpent;
    public final int newAd;
    public final int sdEarned;
    public final int sdFromSkillUse;
    public final int newSd;

    public ManeuverOutput(boolean success, int numSuccs, int numRolls,
                          int adSpent, int newAd, int sdEarned,
                          int sdFromSkillUse, int newSd) {
      this.success = success;
      this.numSuccs = numSuccs;
      this.numRolls = numRolls;
      this.adSpent = adSpent;
      this.newAd = newAd;
      this.sdEarned = sdEarned;
      this.sdFromSkillUse = sdFromSkillUse;
      this.newSd = newSd;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The maneuver succeeds (%d/%d): AD-%d=%d SD+%d+%d=%d",
                             numSuccs, numRolls, adSpent, newAd, sdEarned, sdFromSkillUse, newSd);
      } else {
        return String.format("The maneuver fails (%d/%d). No AD are spent. SD+%d=%d",
                             numSuccs, numRolls, sdFromSkillUse, newSd);
      }
    }
  }

  public ManeuverOutput maneuver(ManeuverInput input) {
    int nd = input.stats.getRating(input.skill) + input.ad +
        input.stats.getManeuverBonus();

    int[] rolls = roll(nd);
    int sdEarned = succs(rolls);
    boolean totalFailure = sdEarned == 0;

    if (!totalFailure) {
      input.stats.addAd(-input.ad);
      if (sdEarned > 5) {
        sdEarned = 5;
      }
      input.stats.addSd(sdEarned);
    }

    int sdFromSkillUse = 0;
    if (input.stats.getSkills().size() == 3 &&
        input.stats.allSkillsUsed()) { // prior to maneuver start
      sdFromSkillUse = 2;
      input.stats.resetSkillUses();
    }
    input.stats.useSkill(input.skill);
    input.stats.addSd(sdFromSkillUse);

    return new ManeuverOutput(!totalFailure, sdEarned, nd,
                              totalFailure ? 0 : input.ad, input.stats.getAd(),
                              sdEarned, sdFromSkillUse, input.stats.getSd());
  }

  public static class StrikeInput implements Input {
    public final Stats stats;
    public final int sd;
    public final Stats defenderStats;

    public StrikeInput(Stats stats, int sd, Stats defenderStats) {
      this.stats = Objects.requireNonNull(stats);
      Preconditions.checkArgument(sd >= 1 && sd <= 6,
                                  "sd must be between 1 and 6");
      Preconditions.checkArgument(stats.getSd() >= sd,
                                  "Not enough strike dice");
      this.sd = sd;
      this.defenderStats = Objects.requireNonNull(defenderStats);
    }
  }

  public static class StrikeOutput implements Output {
    public final boolean success;
    public final int numSuccs;
    public final int numRolls;
    public final int defense;
    public final int sdSpent;
    public final int newSd;
    public final int numWounds;

    public StrikeOutput(boolean success, int numSuccs, int numRolls, int defense,
                        int sdSpent, int newSd, int numWounds) {
      this.success = success;
      this.numSuccs = numSuccs;
      this.numRolls = numRolls;
      this.defense = defense;
      this.sdSpent = sdSpent;
      this.newSd = newSd;
      this.numWounds = numWounds;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The strike succeeds (%d/%d vs. %d): SD-%d=%d Wounds=%d",
                             numSuccs, numRolls, defense, sdSpent, newSd, numWounds);
      } else {
        return String.format("The strike fails (%d/%d vs. %d). No SD are spent.",
                             numSuccs, numRolls, defense);
      }
    }
  }

  public StrikeOutput strike(StrikeInput input) {
    int nd = input.sd + input.stats.getStrikeBonus();

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    int defense = input.defenderStats.getDefense();
    int numWounds = attack / defense;
    boolean success = numWounds > 0;
    if (success) {
      input.stats.addSd(-input.sd);
      input.defenderStats.wound(numWounds);
    }

    return new StrikeOutput(success, attack, nd, defense,
                            success ? input.sd : 0, input.stats.getSd(),
                            numWounds);
  }

  public static class SkillActionInput implements Input {
    public final Stats stats;
    public final int sd;
    public final Skill skill;
    public final Stats dStats;

    public SkillActionInput(Stats stats, int sd, Skill skill, Stats dStats) {
      this.stats = Objects.requireNonNull(stats);
      Preconditions.checkArgument(sd >= 0 && sd <= 6,
                                  "sd must be between 0 and 6");
      Preconditions.checkArgument(stats.getSd() >= sd,
                                  "Not enough strike dice");
      this.sd = sd;
      this.skill = Objects.requireNonNull(skill);
      if (!skill.targetsSelf()) {
        this.dStats = Objects.requireNonNull(dStats);
      } else {
        this.dStats = null;
      }
    }
  }

  public static class SkillActionOutput implements Output {
    public final boolean success;
    public final int numSuccs;
    public final int numRolls;
    public final int difficulty;
    public final int sdSpent;
    public final int newSd;
    public final Stats newStats;
    public final Stats newDStats;

    public SkillActionOutput(boolean success, int numSuccs, int numRolls,
                             int difficulty, int sdSpent, int newSd,
                             Stats newStats, Stats newDStats) {
      this.success = success;
      this.numSuccs = numSuccs;
      this.numRolls = numRolls;
      this.difficulty = difficulty;
      this.sdSpent = sdSpent;
      this.newSd = newSd;
      this.newStats = newStats;
      this.newDStats = newDStats;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The skill action succeeds (%d/%d vs %d): SD-%d=%d",
                             numSuccs, numRolls, difficulty, sdSpent, newSd);
      } else {
        return String.format("The skill action fails (%d/%d vs %d). No SD were spent.",
                             numSuccs, numRolls, difficulty);
      }
    }
  }

  public SkillActionOutput skill(SkillActionInput input) {
    int nd = input.sd + input.stats.getRating(input.skill) - 2;

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    int difficulty = input.skill.getActionDifficulty();
    boolean success = attack >= difficulty;
    Stats newStats = null;
    Stats newDStats = null;
    if (success) {
      input.stats.addSd(-input.sd);

      if (input.skill.targetsSelf()) {
        newStats = input.skill.applyStatsFunction(input.stats);
      } else {
        newDStats = input.skill.applyStatsFunction(input.dStats);
      }
    }

    int newSd = newStats != null ? newStats.getSd() : input.stats.getSd();
    return new SkillActionOutput(success, attack, nd, difficulty,
                                 success ? input.sd : 0, newSd,
                                 newStats, newDStats);
  }

  public static class CatchBreathInput implements Input {
    public final Stats stats;

    public CatchBreathInput(Stats stats) {
      this.stats = Objects.requireNonNull(stats);
    }
  }

  public static class CatchBreathOutput implements Output {
    public final int adEarned;
    public final int newAd;

    public CatchBreathOutput(int adEarned, int newAd) {
      this.adEarned = adEarned;
      this.newAd = newAd;
    }

    @Override
    public String formatResult() {
      return String.format("Catching breath succeeds: AD+%d=%d", adEarned, newAd);
    }
  }

  public CatchBreathOutput catchBreath(CatchBreathInput input) {
    input.stats.addAd(3);

    return new CatchBreathOutput(3, input.stats.getAd());
  }

  @VisibleForTesting
  int[] roll(int n) {
    ThreadLocalRandom r = ThreadLocalRandom.current();
    int[] rolls = new int[n];
    for (int i = 0; i < n; i++) {
      rolls[i] = r.nextInt(1, 7);
    }
    return rolls;
  }

  @VisibleForTesting
  static int succs(int[] rolls) {
    int sum = 0;
    for (int i = 0; i < rolls.length; i++) {
      if (succ(rolls[i])) {
        sum++;
      }
    }
    return sum;
  }

  private static boolean succ(int n) {
    return n >= 3;
  }
}
