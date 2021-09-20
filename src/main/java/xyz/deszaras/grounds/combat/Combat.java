package xyz.deszaras.grounds.combat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.combat.grapple.GrappleSystem;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * A thing that tracks combat in a location. It holds an {@link Engine} which
 * runs the combat, but this thing serves as its connection to the universe.
 * Combat commands work with the engine through the combat.
 */
public class Combat extends Thing {

  private static final Logger LOG = LoggerFactory.getLogger(Combat.class);

  private final System system;

  private Map<String, Team.Builder> teamBuilders;
  private Engine engine;

  /**
   * Creates a new combat.
   *
   * @param  name combat name
   */
  public Combat(String name) {
    this(name, new GrappleSystem());
  }

  @VisibleForTesting
  Combat(String name, System system) {
    super(name);
    this.system = Objects.requireNonNull(system);

    teamBuilders = new HashMap<>();
    engine = null;
  }

  /**
   * Creates a new combat.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Combat(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);
    system = new GrappleSystem();

    teamBuilders = new HashMap<>();
    engine = null;
  }

  /**
   * Gets the system being used for combat.
   *
   * @return combat system
   */
  public System getSystem() {
    return system;
  }

  @VisibleForTesting
  Engine getEngine() {
    return engine;
  }

  @VisibleForTesting
  void setEngine(Engine engine) {
    this.engine = engine;
  }

  private void failIfStarted() {
    if (engine != null) {
      throw new IllegalStateException("Combat has already started");
    }
  }

  private void failIfNotStarted() {
    if (engine == null) {
      throw new IllegalStateException("Combat has not yet started");
    }
  }

  /**
   * Adds a new player as a member of a team.
   *
   * @param  player   player to add
   * @param  teamName team name
   * @return          this combat
   * @throws IllegalStateException if combat has started
   */
  public Combat addPlayer(Player player, String teamName) {
    LOG.debug("Adding player {} to {}", player.getName(), teamName);
    failIfStarted();
    Team.Builder teamBuilder =
        teamBuilders.computeIfAbsent(teamName,
                                     n -> system.getTeamBuilder(n));
    teamBuilder.member(player);
    return this;
  }

  /**
   * Removes a player as a member of a team.
   *
   * @param  player   player to remove
   * @param  teamName team name
   * @return          this combat
   * @throws IllegalStateException if combat has started
   */
  public Combat removePlayer(Player player, String teamName) {
    LOG.debug("Removing player {} from {}", player.getName(), teamName);
    failIfStarted();
    if (teamBuilders.containsKey(teamName)) {
      Team.Builder teamBuilder = teamBuilders.get(teamName);
      teamBuilder.removeMember(player);
    }
    return this;
  }

  /**
   * Removes a named player as a member of a team.
   *
   * @param  playerName   name of player to remove
   * @param  teamName     team name
   * @return              this combat
   * @throws IllegalStateException if combat has started
   */
  public Combat removePlayer(String playerName, String teamName) {
    LOG.debug("Removing player {} from {}", playerName, teamName);
    failIfStarted();
    if (teamBuilders.containsKey(teamName)) {
      Team.Builder teamBuilder = teamBuilders.get(teamName);
      teamBuilder.removeMember(playerName);
    }
    return this;
  }

  /**
   * Starts combat. Teams move in the listed priority order; any teams not
   * listed are prioritized after them, in arbitrary order.
   *
   * @param  teamNames names of teams, in priority order
   * @return           this combat
   * @throws IllegalStateException if combat has started
   */
  public Combat start(List<String> teamNames) {
    LOG.debug("Starting combat");
    failIfStarted();
    Engine.Builder engineBuilder = system.getEngineBuilder();

    // Add teams in priority order.
    teamNames.stream().forEach(teamName -> {
      if (!teamBuilders.containsKey(teamName)) {
        throw new IllegalArgumentException("Team " + teamName + " does not exist");
      }
      engineBuilder.addTeam(teamBuilders.get(teamName).build());
    });

    // Add any teams left that weren't in the list, in arbitrary order.
    for (Map.Entry<String, Team.Builder> e : teamBuilders.entrySet()) {
      if (!teamNames.contains(e.getKey())) {
        engineBuilder.addTeam(e.getValue().build());
      }
    }

    engine = engineBuilder.build();
    engine.start();

    return this;
  }

  /**
   * Ends combat. Combat can be ended even if it hasn't started, to cancel it.
   */
  public void end() {
    LOG.debug("Ending combat");
    if (engine != null) {
      engine.end();
    }
  }

  @VisibleForTesting
  static final String STATUS_NO_PLAYERS =
      "Combat has not yet started. No players have been added yet.";
  @VisibleForTesting
  static final String STATUS_PRE_START =
      "Players are still being added.";

  /**
   * Returns the status of this combat. If combat has started, I love my wife.
   * Otherwise, the status reports on which players have been added to teams.
   *
   * @return status
   */
  public String status() {
    if (engine == null) {
      if (teamBuilders.isEmpty()) {
        return STATUS_NO_PLAYERS;
      }
      StringBuilder b = new StringBuilder(STATUS_PRE_START);
      for (Team.Builder tb : teamBuilders.values()) {
        b.append("\n- ").append(tb.status());
      }
      return b.toString();
    }
    return engine.status();
  }

  public String move(String playerName, List<String> command) {
    failIfNotStarted();
    return engine.move(playerName, command);
  }

  public String move(Player player, List<String> command) {
    failIfNotStarted();
    return engine.move(player, command);
  }

  public String resolveRound() {
    failIfNotStarted();
    return engine.resolveRound();
  }

  @JsonIgnore
  public Set<Player> getAllCombatants() {
    Set<Player> allCombatants = new HashSet<>();
    if (engine == null) {
      for (Team.Builder tb : teamBuilders.values()) {
        allCombatants.addAll(tb.getMembers());
      }
    } else {
      for (Team team : engine.getTeams()) {
        allCombatants.addAll(team.getMembers());
      }
    }
    return ImmutableSet.copyOf(allCombatants);
  }
}
