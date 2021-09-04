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
    public final int adSpent;
    public final int sdEarned;

    public ManeuverOutput(boolean success, int adSpent, int sdEarned) {
      this.success = success;
      this.adSpent = adSpent;
      this.sdEarned = sdEarned;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The maneuver succeeds. AD spent: %d. SD earned: %d",
                             adSpent, sdEarned);
      } else {
        return "The maneuver fails. No AD are spent, and no SD are earned.";
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

    return new ManeuverOutput(!totalFailure, totalFailure ? 0 : input.ad,
                              sdEarned);
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
    public final int sdSpent;
    public final int numSuccs;
    public final int numWounds;

    public StrikeOutput(boolean success, int sdSpent, int numSuccs,
                          int numWounds) {
      this.success = success;
      this.sdSpent = sdSpent;
      this.numSuccs = numSuccs;
      this.numWounds = numWounds;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The strike succeeds. SD spent: %d. Successes: %d. Wounds: %d",
                             sdSpent, numSuccs, numWounds);
      } else {
        return "The strike fails. No SD are spent.";
      }
    }
  }

  public StrikeOutput strike(StrikeInput input) {
    int nd = input.sd + input.stats.getStrikeBonus();

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    int numWounds = attack / input.defenderStats.getDefense();
    boolean success = numWounds > 0;
    if (success) {
      input.stats.addSd(-input.sd);
      input.defenderStats.wound(numWounds);
    }

    return new StrikeOutput(success, success ? input.sd : 0, attack, numWounds);
  }

  public static class SkillActionInput implements Input {
    public final Stats stats;
    public final int sd;
    public final Skill skill;

    public SkillActionInput(Stats stats, int sd, Skill skill) {
      this.stats = Objects.requireNonNull(stats);
      Preconditions.checkArgument(sd >= 0 && sd <= 6,
                                  "sd must be between 0 and 6");
      Preconditions.checkArgument(stats.getSd() >= sd,
                                  "Not enough strike dice");
      this.sd = sd;
      this.skill = Objects.requireNonNull(skill);
    }
  }

  public static class SkillActionOutput implements Output {
    public final boolean success;
    public final int sdSpent;
    public final int numSuccs;

    public SkillActionOutput(boolean success, int sdSpent, int numSuccs) {
      this.success = success;
      this.sdSpent = sdSpent;
      this.numSuccs = numSuccs;
    }

    @Override
    public String formatResult() {
      if (success) {
        return String.format("The skill action succeeds. SD spent: %d. Successes: %d",
                             sdSpent, numSuccs);
      } else {
        return "The skill action fails. No SD were spent.";
      }
    }
  }

  public SkillActionOutput skill(SkillActionInput input) {
    int nd = input.sd + input.stats.getRating(input.skill) - 2;

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    boolean success = attack >= input.skill.getActionDifficulty();
    if (success) {
      input.stats.addSd(-input.sd);
    }

    return new SkillActionOutput(success, success ? input.sd : 0, attack);
  }

  public static class CatchBreathInput implements Input {
    public final Stats stats;

    public CatchBreathInput(Stats stats) {
      this.stats = Objects.requireNonNull(stats);
    }
  }

  public static class CatchBreathOutput implements Output {
    public final int adEarned;

    public CatchBreathOutput(int adEarned) {
      this.adEarned = adEarned;
    }

    @Override
    public String formatResult() {
      return String.format("Catching breath succeeds. AD earned: %d.", adEarned);
    }
  }

  public CatchBreathOutput catchBreath(CatchBreathInput input) {
    input.stats.addAd(2);

    return new CatchBreathOutput(2);
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
