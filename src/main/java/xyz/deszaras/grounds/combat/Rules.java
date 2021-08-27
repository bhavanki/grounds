package xyz.deszaras.grounds.combat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Rules {

  public static class ManeuverOutcome {

    public final boolean success;
    public final int adSpent;
    public final int sdEarned;

    private ManeuverOutcome(boolean success, int adSpent, int sdEarned) {
      this.success = success;
      this.adSpent = adSpent;
      this.sdEarned = sdEarned;
    }
  }

  public ManeuverOutcome maneuver(Stats s, Skill skill, int ad) {
    Preconditions.checkArgument(s.getAd() >= ad, "Not enough action dice");
    Preconditions.checkArgument(ad >= 1 && ad <= 3, "ad must be between 1 and 3");

    s.addAd(-ad);

    int nd = s.getRating(skill) + ad;

    int[] rolls = roll(nd);
    int sdEarned = succs(rolls);
    boolean totalFailure = sdEarned == 0;

    if (totalFailure) {
      s.addAd(ad);
    } else {
      if (sdEarned > 5) {
        sdEarned = 5;
      }
      s.addSd(sdEarned);
    }

    return new ManeuverOutcome(!totalFailure, totalFailure ? 0 : ad, sdEarned);
  }

  public static class StrikeOutcome {

    public final boolean success;
    public final int sdSpent;
    public final int numSuccs;
    public final int numWounds;

    private StrikeOutcome(boolean success, int sdSpent, int numSuccs,
                          int numWounds) {
      this.success = success;
      this.sdSpent = sdSpent;
      this.numSuccs = numSuccs;
      this.numWounds = numWounds;
    }
  }

  public StrikeOutcome strike(Stats s, int sd, Stats d) {
    Preconditions.checkArgument(s.getSd() >= sd, "Not enough strike dice");
    Preconditions.checkArgument(sd >= 1 && sd <= 6, "sd must be between 1 and 6");

    s.addSd(-sd);

    int nd = sd;

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    int numWounds = attack / d.getDefense();
    boolean success = numWounds > 0;
    if (success) {
      d.wound(numWounds);
    } else {
      s.addSd(sd);
    }

    return new StrikeOutcome(success, success ? sd : 0, attack, numWounds);
  }

  public static class SkillActionOutcome {

    public final boolean success;
    public final int sdSpent;
    public final int numSuccs;

    private SkillActionOutcome(boolean success, int sdSpent, int numSuccs) {
      this.success = success;
      this.sdSpent = sdSpent;
      this.numSuccs = numSuccs;
    }
  }

  public SkillActionOutcome skill(Stats s, int sd, Skill skill) {
    Preconditions.checkArgument(s.getSd() >= sd, "Not enough strike dice");
    Preconditions.checkArgument(sd >= 0 && sd <= 6, "sd must be between 0 and 6");

    s.addSd(-sd);

    int nd = sd + s.getRating(skill) - 2;

    int[] rolls = roll(nd);
    int attack = succs(rolls);
    boolean success = attack >= skill.getActionDifficulty();
    if (!success) {
      s.addSd(sd);
    }

    return new SkillActionOutcome(success, success ? sd : 0, attack);
  }

  public static class CatchBreathOutcome {

    public final int adEarned;

    private CatchBreathOutcome(int adEarned) {
      this.adEarned = adEarned;
    }
  }

  public CatchBreathOutcome catchBreath(Stats s) {
    s.addAd(2);

    return new CatchBreathOutcome(2);
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

  private static int succs(int[] rolls) {
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
