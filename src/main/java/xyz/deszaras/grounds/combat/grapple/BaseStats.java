package xyz.deszaras.grounds.combat.grapple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseStats implements Stats {

  private final Map<Skill, Integer> skills;
  private final Map<Skill, Boolean> skillUses;
  private final int apMaxSize;
  private final int defense;
  private final int maxWounds;

  private int ad;
  private int sd;
  private int wounds;

  private BaseStats(Map<Skill, Integer> skills, int apMaxSize, int defense,
                    int maxWounds) {
    this.skills = ImmutableMap.copyOf(skills);
    this.skillUses = new HashMap<>();
    for (Skill sk : skills.keySet()) {
      skillUses.put(sk, false);
    }
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
  public Map<Skill, Boolean> getSkillUses() {
    return ImmutableMap.copyOf(skillUses);
  }

  @Override
  public void useSkill(Skill sk) {
    skillUses.put(sk, true);
  }

  private void setSkillUse(Skill sk, boolean used) {
    skillUses.put(sk, used);
  }

  @Override
  public boolean isUsed(Skill sk) {
    return skillUses.get(sk);
  }

  @Override
  public boolean allSkillsUsed() {
    return skillUses.values().stream().allMatch(u -> u);
  }

  @Override
  public void resetSkillUses() {
    for (Skill sk : skillUses.keySet()) {
      skillUses.put(sk, false);
    }
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

  @Override
  public boolean isLegal() {
    // The builder ensures only legal instances may be constructed.
    return true;
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
    private int apMaxSize = Limits.MIN_AP_MAX_SIZE;
    private int defense = Limits.MIN_DEFENSE;
    private int maxWounds = Limits.MIN_MAX_WOUNDS;
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
      Preconditions.checkArgument(apMaxSize >= Limits.MIN_AP_MAX_SIZE,
                                  "apMaxSize must be at least " +
                                  Limits.MIN_AP_MAX_SIZE);
      Preconditions.checkArgument(apMaxSize <= Limits.MAX_AP_MAX_SIZE,
                                  "apMaxSize must be no more than " +
                                  Limits.MAX_AP_MAX_SIZE);
      this.apMaxSize = apMaxSize;
      return this;
    }

    public Builder defense(int defense) {
      Preconditions.checkArgument(defense >= Limits.MIN_DEFENSE,
                                  "defense must be at least " +
                                  Limits.MIN_DEFENSE);
      Preconditions.checkArgument(defense <= Limits.MAX_DEFENSE,
                                  "defense must be no more than " +
                                  Limits.MAX_DEFENSE);
      this.defense = defense;
      return this;
    }

    public Builder maxWounds(int maxWounds) {
      Preconditions.checkArgument(maxWounds >= Limits.MIN_MAX_WOUNDS,
                                  "maxWounds must be at least " +
                                  Limits.MIN_MAX_WOUNDS);
      Preconditions.checkArgument(maxWounds <= Limits.MAX_MAX_WOUNDS,
                                  "maxWounds must be no more than " +
                                  Limits.MAX_MAX_WOUNDS);
      this.maxWounds = maxWounds;
      return this;
    }

    public Builder npc() {
      this.npc = true;
      return this;
    }

    public BaseStats build() {
      validateSkills();
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

  public ProtoModel.BaseStats toBaseStatsProto() {
    return ProtoModel.BaseStats.newBuilder()
        .putAllSkills(skills.entrySet().stream()
                      .collect(Collectors.toMap(e -> e.getKey().getName(),
                                                e -> e.getValue())))
        .putAllSkillUses(skillUses.entrySet().stream()
                         .collect(Collectors.toMap(e -> e.getKey().getName(),
                                                   e -> e.getValue())))
        .setApMaxSize(apMaxSize)
        .setDefense(defense)
        .setMaxWounds(maxWounds)
        .setAd(ad)
        .setSd(sd)
        .setWounds(wounds)
        .build();
  }

  @Override
  public ProtoModel.Stats toProto() {
    return ProtoModel.Stats.newBuilder()
        .setBaseStats(toBaseStatsProto())
        .build();
  }

  public static BaseStats fromProto(ProtoModel.BaseStats proto) {
    Builder builder = BaseStats.builder()
        .apMaxSize(proto.getApMaxSize())
        .defense(proto.getDefense())
        .maxWounds(proto.getMaxWounds());
    for (Map.Entry<String, Integer> e : proto.getSkillsMap().entrySet()) {
      builder.skill(Skills.forName(e.getKey()), e.getValue());
    }
    BaseStats baseStats = builder.build();
    baseStats.init(proto.getAd(), proto.getSd(), proto.getWounds());
    for (Map.Entry<String, Boolean> e : proto.getSkillUsesMap().entrySet()) {
      baseStats.setSkillUse(Skills.forName(e.getKey()), e.getValue());
    }
    return baseStats;
  }
}
