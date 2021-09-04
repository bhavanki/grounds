package xyz.deszaras.grounds.combat;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.function.Function;

/**
 * A skill in the Grapple combat system.
 */
public class Skill {

  private final String name;
  private final int actionDifficulty;
  private final boolean targetsSelf;
  private final Function<Stats, Stats> statsFunction;

  /**
   * Creates a new skill.
   *
   * @param  name             name
   * @param  actionDifficulty difficulty for skill action (number of succs)
   * @param  targetsSelf      true if skill action targets self
   * @param  statsFunction    function to apply to target stats on action success
   */
  public Skill(String name, int actionDifficulty, boolean targetsSelf,
               Function<Stats, Stats> statsFunction) {
    this.name = Objects.requireNonNull(name, "name must not be null");
    Preconditions.checkArgument(actionDifficulty > 0,
                                "actionDifficulty must be positive");
    this.actionDifficulty = actionDifficulty;
    this.targetsSelf = targetsSelf;
    if (statsFunction != null) {
      this.statsFunction = statsFunction;
    } else {
      this.statsFunction = Function.identity();
    }
  }

  /**
   * Gets the skill name.
   *
   * @return skill name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the skill action difficulty. This is the minimum number of successful
   * rolls needed for the action to succeed.
   *
   * @return skill action difficulty
   */
  public int getActionDifficulty() {
    return actionDifficulty;
  }

  /**
   * Returns whether this skill's action targets the one performing the action
   * or some external target.
   *
   * @return true if this skill's action targets the one performing it
   */
  public boolean targetsSelf() {
    return targetsSelf;
  }

  /**
   * Applies the skill action's stats function to the given stats. Some skills
   * change values in the stats, while others alter its behavior.
   *
   * @param  stats target stats
   * @return       new stats for target (which may be the original object)
   */
  public Stats applyStatsFunction(Stats stats) {
    return statsFunction.apply(stats);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Skill that = (Skill) o;
    if (!name.equals(that.name)) {
      return false;
    }
    if (actionDifficulty != that.actionDifficulty) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, actionDifficulty);
  }
}
