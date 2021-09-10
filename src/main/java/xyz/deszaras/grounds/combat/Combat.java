package xyz.deszaras.grounds.combat;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Policy;
// TODO: See if the Team concept does not need to be leaked from Engine
import xyz.deszaras.grounds.combat.Engine.Team;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class Combat extends Thing {

  private static final Logger LOG = LoggerFactory.getLogger(Combat.class);

  private Map<String, Engine.Team.Builder> teamBuilders;
  private Engine engine;

  public Combat(String name) {
    super(name);

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

    teamBuilders = new HashMap<>();
    engine = null;
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

  public void addPlayer(Player player, String teamName) {
    LOG.debug("Adding player {} to {}", player.getName(), teamName);
    failIfStarted();
    Engine.Team.Builder teamBuilder =
        teamBuilders.computeIfAbsent(teamName, n -> Engine.Team.builder(n));
    teamBuilder.member(player);
  }

  public void removePlayer(Player player, String teamName) {
    LOG.debug("Removing player {} from {}", player.getName(), teamName);
    failIfStarted();
    if (teamBuilders.containsKey(teamName)) {
      Engine.Team.Builder teamBuilder = teamBuilders.get(teamName);
      teamBuilder.removeMember(player);
    }
  }

  public void removePlayer(String playerName, String teamName) {
    LOG.debug("Removing player {} from {}", playerName, teamName);
    failIfStarted();
    if (teamBuilders.containsKey(teamName)) {
      Engine.Team.Builder teamBuilder = teamBuilders.get(teamName);
      teamBuilder.removeMember(playerName);
    }
  }

  public void start() {
    LOG.debug("Starting combat");
    failIfStarted();
    Engine.Builder engineBuilder = Engine.builder();
    for (Engine.Team.Builder tb : teamBuilders.values()) {
      engineBuilder.addTeam(tb.build());
    }
    engine = engineBuilder.build();
    engine.start();
  }

  public void end() {
    LOG.debug("Ending combat");
    failIfNotStarted();
    engine.end();
  }

  public String status() {
    if (engine == null) {
      if (teamBuilders.isEmpty()) {
        return "Combat has not yet started. No players have been added yet.";
      }
      StringBuilder b = new StringBuilder("Players are still being added.");
      for (Team.Builder tb : teamBuilders.values()) {
        b.append("\n- ").append(tb.status());
      }
      return b.toString();
    }
    return engine.status();
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

  public static Combat build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 0, "Expected 0 build arguments, got " + buildArgs.size());
    return new Combat(name);
  }
}
