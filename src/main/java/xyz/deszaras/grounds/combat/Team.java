package xyz.deszaras.grounds.combat;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.model.Player;

/**
 * A team is a named set of players.
 */
class Team {

  private final String name;
  private final Map<Player, Stats> members;

  private Team(String name, Map<Player, Stats> members) {
    this.name = Objects.requireNonNull(name);
    this.members = new HashMap<>(members);
  }

  /**
   * Gets the team name.
   *
   * @return team name
   */
  String getName() {
    return name;
  }

  /**
   * Determines if the given player is a member of this team.
   *
   * @param  player player
   * @return        true if player is a member of this team
   */
  boolean isMember(Player player) {
    return members.containsKey(player);
  }

  /**
   * Gets a team member by name.
   *
   * @param  playerName player name
   * @return            player on team, or empty if not present
   */
  Optional<Player> getMemberByName(String playerName) {
    return members.keySet().stream()
        .filter(p -> p.getName().equals(playerName))
        .findFirst();
  }

  /**
   * Gets the team members.
   *
   * @return team members
   */
  Set<Player> getMembers() {
    return ImmutableSet.copyOf(members.keySet());
  }

  /**
   * Gets the stats for the given player.
   *
   * @param  player player
   * @return        stats
   * @throws IllegalArgumentException if the player is not a member of this team
   */
  Stats getMemberStats(Player player) {
    if (!isMember(player)) {
      throw new IllegalArgumentException("Player " + player.getName() +
                                         " is not a member of team " +
                                         name);
    }
    return members.get(player);
  }

  /**
   * Sets the stats for the given player.
   *
   * @param  player player
   * @param  stats  stats
   * @throws IllegalArgumentException if the player is not a member of this team
   */
  void setMemberStats(Player player, Stats stats) {
    if (!isMember(player)) {
      throw new IllegalArgumentException("Player " + player.getName() +
                                         " is not a member of team " +
                                         name);
    }
    members.put(player, stats);
  }

  /**
   * Determines if all team members are out.
   *
   * @return true if all team members are out
   */
  boolean isOut() {
    return members.values().stream().allMatch(Stats::isOut);
  }

  /**
   * Gets a builder for a new team.
   *
   * @param  name team name
   * @return builder
   */
  static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * A builder for {@link Team} objects.
   */
  static class Builder {
    private String name;
    private Map<Player, Stats> members;

    private Builder(String name) {
      this.name = Objects.requireNonNull(name);
      members = new HashMap<>();
    }

    /**
     * Sets the team name.
     *
     * @param  name team name
     * @return      this builder
     */
    Builder name(String name) {
      this.name = Objects.requireNonNull(name);
      return this;
    }

    /**
     * Adds a new player to this team. Their stats are computed from their
     * attributes.
     *
     * @param  player player to add as member
     * @return        this builder
     * @throws IllegalArgumentException if the player is missing an attribute,
     *         of if the player is already a member of this team
     */
    Builder member(Player player) {
      if (members.containsKey(player)) {
        throw new IllegalArgumentException(player.getName() + " is already a member of team " +
                                           name);
      }
      members.put(player, Engine.buildStats(player));
      return this;
    }

    /**
     * Removes a player from this team.
     *
     * @param  player player to remove as member
     * @return        this builder
     */
    Builder removeMember(Player player) {
      if (members.containsKey(player)) {
        members.remove(player);
      }
      return this;
    }

    /**
     * Removes a player from this team.
     *
     * @param  playerName name of player to remove as member
     * @return            this builder
     */
    Builder removeMember(String playerName) {
      members.keySet().stream()
          .filter(p -> p.getName().equals(playerName))
          .findFirst().ifPresent(p -> members.remove(p));
      return this;
    }

    /**
     * Gets the current members of this team.
     *
     * @return team members
     */
    Set<Player> getMembers() {
      return members.keySet();
    }

    /**
     * Builds a new team.
     *
     * @return new team
     * @throws IllegalStateException if there are no members
     */
    Team build() {
      Preconditions.checkState(members.size() >= 1,
                               "At least one team member is required");
      return new Team(name, members);
    }

    /**
     * Returns the status of this team so far.
     *
     * @return team status
     */
    public String status() {
      StringBuilder b = new StringBuilder(name).append(":");
      if (members.isEmpty()) {
        b.append(" no members yet");
      } else {
        b.append(" ");
        b.append(members.keySet().stream()
                 .map(p -> p.getName())
                 .collect(Collectors.joining(", ")));
      }
      return b.toString();
    }
  }
}
