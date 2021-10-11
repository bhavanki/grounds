package xyz.deszaras.grounds.combat;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.model.Player;

/**
 * An engine runs combat under some system. It keeps track of a list of teams
 * and provides the basic functions for performing combat among them.<p>
 *
 * An implementation of this class follows a specific set of rules for a combat
 * system. Some functions offered by this class may not apply to some systems.
 */
public abstract class Engine {

  protected final List<? extends Team> teams;

  /**
   * Creates a new engine.
   *
   * @param  teams teams
   */
  protected Engine(List<? extends Team> teams) {
    this.teams = ImmutableList.copyOf(teams);
  }

  /**
   * Gets the teams tracked by this engine.
   *
   * @return teams
   */
  public List<Team> getTeams() {
    return ImmutableList.copyOf(teams);
  }

  /**
   * Starts combat.
   *
   * @param  teamNames names of teams, in priority order
   * @return           this combat
   * @throws IllegalStateException if combat has started
   */
  public abstract void start();

  /**
   * Ends combat. Combat can be ended even if it hasn't started, to cancel it.
   */
  public abstract void end();

  /**
   * Returns the status of this combat.
   *
   * @return status
   */
  public abstract String status();

  /**
   * Executes a player move.
   *
   * @param  playerName name of moving player
   * @param  command    move command (content depends on engine implementation)
   * @return            description of move result
   * @throws IllegalArgumentException if the named player is not in combat
   */
  public String move(String playerName, List<String> command) {
    Optional<Player> player = teams.stream()
        .flatMap(t -> t.getMembers().stream())
        .filter(p -> p.getName().equals(playerName))
        .findFirst();
    if (player.isPresent()) {
      return move(player.get(), command);
    }
    throw new IllegalArgumentException("No player " + playerName +
                                       " is participating in combat");
  }

  /**
   * Executes a player move.
   *
   * @param  playerName moving player
   * @param  command    move command (content depends on engine implementation)
   * @return            description of move result
   * @throws IllegalArgumentException if the player is not in combat
   */
  public abstract String move(Player p, List<String> command);

  /**
   * Resolves a round of combat.
   *
   * @return description of round resolution
   */
  public abstract String resolveRound();

  /**
   * Gets the state of combat, such that it can be restored later. Optional
   * operation; the default implementation returns null.
   *
   * @return engine state
   */
  public byte[] getState() {
    return null;
  }

  public abstract static class Builder {

    public abstract Builder addTeam(Team team);

    public abstract Engine build();

    public abstract String status();
  }
}
