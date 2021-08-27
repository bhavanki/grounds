package xyz.deszaras.grounds.combat;

import com.google.common.base.Preconditions;

import java.util.Objects;

public class Skill {

  private final String name;
  private final int actionDifficulty;

  public Skill(String name, int actionDifficulty) {
    this.name = Objects.requireNonNull(name, "name must not be null");
    Preconditions.checkArgument(actionDifficulty > 0,
                                "actionDifficulty must be positive");
    this.actionDifficulty = actionDifficulty;
  }

  public String getName() {
    return name;
  }

  public int getActionDifficulty() {
    return actionDifficulty;
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
