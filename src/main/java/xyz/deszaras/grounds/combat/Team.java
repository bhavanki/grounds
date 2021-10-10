package xyz.deszaras.grounds.combat;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import xyz.deszaras.grounds.model.Player;

/**
 * A team is a named set of players.
 */
public abstract class Team {

  protected final String name;

  protected Team(String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Gets the team name.
   *
   * @return team name
   */
  public String getName() {
    return name;
  }

  /**
   * Determines if the given player is a member of this team.
   *
   * @param  player player
   * @return        true if player is a member of this team
   */
  public abstract boolean isMember(Player player);

  /**
   * Gets a team member by name.
   *
   * @param  playerName player name
   * @return            player on team, or empty if not present
   */
  public abstract Optional<Player> getMemberByName(String playerName);

  /**
   * Gets the team members.
   *
   * @return team members
   */
  public abstract Set<Player> getMembers();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Team that = (Team) o;
    if (!name.equals(that.name)) {
      return false;
    }
    if (!getMembers().equals(that.getMembers())) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, getMembers());
  }

  /**
   * A builder for {@link Team} objects.
   */
  public abstract static class Builder {
    protected String name;

    protected Builder(String name) {
      this.name = Objects.requireNonNull(name);
    }

    /**
     * Sets the team name.
     *
     * @param  name team name
     * @return      this builder
     */
    public Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    /**
     * Adds a new player to this team.
     *
     * @param  player player to add as member
     * @return        this builder
     * @throws IllegalArgumentException if the player is already a member of
     * this team
     */
    public abstract Builder member(Player player);

    /**
     * Removes a player from this team.
     *
     * @param  player player to remove as member
     * @return        this builder
     */
    public abstract Builder removeMember(Player player);

    /**
     * Removes a player from this team.
     *
     * @param  playerName name of player to remove as member
     * @return            this builder
     */
    public abstract Builder removeMember(String playerName);

    /**
     * Gets the current members of this team.
     *
     * @return team members
     */
    public abstract Set<Player> getMembers();

    /**
     * Builds a new team.
     *
     * @return new team
     * @throws IllegalStateException if there are no members
     */
    public abstract Team build();

    /**
     * Returns the status of this team so far.
     *
     * @return team status
     */
    public abstract String status();
  }
}
