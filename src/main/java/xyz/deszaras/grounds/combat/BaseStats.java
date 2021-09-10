package xyz.deszaras.grounds.combat;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BaseStats implements Stats {

  private final Map<Skill, Integer> skills;
  private final int apMaxSize;
  private final int defense;
  private final int maxWounds;

  private int ad;
  private int sd;
  private int wounds;

  private BaseStats(Map<Skill, Integer> skills, int apMaxSize, int defense,
                    int maxWounds) {
    this.skills = ImmutableMap.copyOf(skills);
    this.apMaxSize = apMaxSize;
    this.defense = defense;
    this.maxWounds = maxWounds;
  }

  public BaseStats init(int ad, int sd) {
    return init(ad, sd, 0);
  }

  public BaseStats init(int ad, int sd, int wounds) {
    Preconditions.checkArgument(ad <= apMaxSize, "ad may not exceed " + apMaxSize);
    Preconditions.checkArgument(ad >= 0, "ad must be non-negative");
    this.ad = ad;
    this.sd = sd;
    Preconditions.checkArgument(wounds <= maxWounds,
                                "wounds may not exceed " + maxWounds);
    this.wounds = wounds;
    return this;
  }

  @Override
  public Map<Skill, Integer> getSkills() {
    return skills;
  }

  @Override
  public int getRating(Skill sk) {
    if (!skills.containsKey(sk)) {
      throw new IllegalArgumentException("Skill " + sk.getName() + " not present");
    }
    return skills.get(sk);
  }

  @Override
  public int getApMaxSize() {
    return apMaxSize;
  }

  @Override
  public int getAd() {
    return ad;
  }

  @Override
  public int addAd(int amt) {
    int n = ad + amt;
    ad = bound(n, 0, getApMaxSize());
    return ad;
  }

  @Override
  public int setAd(int amt) {
    ad = bound(amt, 0, getApMaxSize());
    return ad;
  }

  @Override
  public int getSd() {
    return sd;
  }

  @Override
  public int addSd(int amt) {
    sd += amt;
    return sd;
  }

  @Override
  public int setSd(int amt) {
    sd = amt;
    return sd;
  }

  @Override
  public int getDefense() {
    return defense;
  }

  @Override
  public int getMaxWounds() {
    return maxWounds;
  }

  @Override
  public int getWounds() {
    return wounds;
  }

  @Override
  public int wound() {
    return wound(1);
  }

  @Override
  public int wound(int amt) {
    int n = wounds + amt;
    wounds = bound(n, 0, getMaxWounds());
    return wounds;
  }

  @Override
  public boolean isOut() {
    return wounds >= getMaxWounds();
  }

  @Override
  public int getManeuverBonus() {
    return 0;
  }

  @Override
  public int getStrikeBonus() {
    return 0;
  }

  private static int bound(int n, int min, int max) {
    if (n < min) {
      return min;
    } else if (n > max) {
      return max;
    } else {
      return n;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private HashMap<Skill, Integer> skills;
    private int apMaxSize;
    private int defense;
    private int maxWounds;
    private boolean npc;

    public Builder skill(Skill skill, int rating) {
      Preconditions.checkArgument(rating >= 2 && rating <= 4,
                                  "rating must be between 2 and 4");
      if (skills == null) {
        skills = new HashMap<>();
      }
      skills.put(skill, rating);
      return this;
    }

    public Builder apMaxSize(int apMaxSize) {
      Preconditions.checkArgument(apMaxSize > 0, "apMaxSize must be positive");
      this.apMaxSize = apMaxSize;
      return this;
    }

    public Builder defense(int defense) {
      Preconditions.checkArgument(defense > 0, "defense must be positive");
      this.defense = defense;
      return this;
    }

    public Builder maxWounds(int maxWounds) {
      Preconditions.checkArgument(maxWounds > 0, "maxWounds must be positive");
      this.maxWounds = maxWounds;
      return this;
    }

    public Builder npc() {
      this.npc = true;
      return this;
    }

    public BaseStats build() {
      validateSkills();
      Preconditions.checkState(apMaxSize > 0, "apMaxSize must be positive");
      Preconditions.checkState(defense > 0, "defense must be positive");
      Preconditions.checkState(maxWounds > 0, "maxWounds must be positive");
      return new BaseStats(skills, apMaxSize, defense, maxWounds);
    }

    private void validateSkills() {
      if (skills == null) {
        throw new IllegalStateException("No skills are set");
      }
      if (!npc) {
        Collection<Integer> values = skills.values();
        if (values.size() != 3) {
          throw new IllegalStateException("Exactly 3 skills are required");
        }
        for (int i = 2; i <= 4; i++) {
          if (!values.contains(i)) {
            throw new IllegalStateException("No skill with rating " + i + " is set");
          }
        }
      }
    }
  }
}
