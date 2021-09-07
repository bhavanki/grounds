package xyz.deszaras.grounds.combat;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.combat.Rules.CatchBreathInput;
// import xyz.deszaras.grounds.combat.Rules.CatchBreathOutput;
// import xyz.deszaras.grounds.combat.Rules.Input;
import xyz.deszaras.grounds.combat.Rules.ManeuverInput;
// import xyz.deszaras.grounds.combat.Rules.ManeuverOutput;
import xyz.deszaras.grounds.combat.Rules.Output;
import xyz.deszaras.grounds.combat.Rules.SkillActionInput;
import xyz.deszaras.grounds.combat.Rules.SkillActionOutput;
import xyz.deszaras.grounds.combat.Rules.StrikeInput;
// import xyz.deszaras.grounds.combat.Rules.StrikeOutput;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiUtils;
import xyz.deszaras.grounds.util.LineOutput;
import xyz.deszaras.grounds.util.TabularOutput;

public class Engine {

  private static final Logger LOG = LoggerFactory.getLogger(Engine.class);

  /**
   * A team is a named set of players.
   */
  static class Team {
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
        members.put(player, buildStats(player));
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

  private static class MoveResult {
    private final String commandResult;
    private final String newMovingTeamName;
    private final Integer newRound;
    private final boolean over;
    private final Team winningTeam;

    private MoveResult(String commandResult, String newMovingTeamName,
                       Integer newRound, boolean over, Team winningTeam) {
      this.commandResult = commandResult;
      this.newMovingTeamName = newMovingTeamName;
      this.newRound = newRound;
      this.over = over;
      this.winningTeam = winningTeam;
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder(commandResult);
      if (over) {
        b.append("\n  COMBAT IS OVER! Winning team: ");
        if (winningTeam != null) {
          b.append(winningTeam.getName());
        } else {
          b.append("none!");
        }
      } else {
        if (newRound != null) {
          b.append("\n  New round:       ").append(newRound);
        }
        if (newMovingTeamName != null) {
          b.append("\n  New team moving: ").append(newMovingTeamName);
        }
      }
      return b.toString();
    }
  }

  private final List<Team> teams;
  private final Rules rules;

  private int round;
  private int movingTeamIndex;
  private Team movingTeam;
  private Set<Player> yetToMove;
  private boolean over;
  private Team winningTeam;

  private Engine(List<Team> teams, Rules rules) {
    this.teams = ImmutableList.copyOf(teams);
    this.rules = rules;
  }

  public List<Team> getTeams() {
    return ImmutableList.copyOf(teams);
  }

  public void start() {
    round = 1;
    movingTeamIndex = 0;
    movingTeam = teams.get(movingTeamIndex);
    yetToMove = new HashSet<>(movingTeam.getMembers());
    over = false;
    winningTeam = null;
  }

  public void end() {
    for (Team team : teams) {
      for (Player player : team.getMembers()) {
        Stats playerStats = team.getMemberStats(player);
        drainStats(player, playerStats);
      }
    }
  }

  private static final int STATUS_WIDTH = 37;

  public String status() {
    String border = new LineOutput(STATUS_WIDTH, "=", Ansi.Color.MAGENTA, true).toString();
    String teamBorder = new LineOutput(STATUS_WIDTH, "- ", Ansi.Color.MAGENTA, true).toString();
    StringBuilder b = new StringBuilder(border).append("\n");

    if (over) {
      b.append(AnsiUtils.color("COMBAT IS OVER", Ansi.Color.RED, true));
    } else {
      b.append("Round: ").append(round);
    }
    for (Team team : teams) {
      b.append("\n").append(teamBorder);
      String teamName = team.getName();
      boolean isMovingTeam = team.equals(movingTeam);
      if (isMovingTeam) {
        teamName += " <-";
      }
      TabularOutput table = new TabularOutput();
      table.defineColumn(teamName, "%-20.20s")
          .defineColumn("AD", "%2s")
          .defineColumn("SD", "%2s")
          .defineColumn("DEF", "%3s")
          .defineColumn("WOUNDS", "%6s");

      for (Player player : team.getMembers()) {
        Stats playerStats = team.getMemberStats(player);
        String playerName = player.getName();
        if (playerStats.isOut()) {
          playerName = AnsiUtils.color(playerName, Ansi.Color.RED, false);
        } else if (isMovingTeam && yetToMove.contains(player)) {
          playerName += " <-";
        }
        String woundString;
        int numWounds = playerStats.getWounds();
        if (playerStats.isOut()) {
          woundString = AnsiUtils.color("X".repeat(numWounds), Ansi.Color.RED, false);
        } else {
          woundString = "*".repeat(numWounds);
        }
        table.addRow(playerName,
                     Integer.toString(playerStats.getAd()),
                     Integer.toString(playerStats.getSd()),
                     Integer.toString(playerStats.getDefense()),
                     woundString);
      }

      b.append("\n").append(table.toString());
    }

    b.append("\n").append(border);
    return b.toString();
  }

  public int getRound() {
    return round;
  }

  public String getMovingTeamName() {
    return movingTeam.getName();
  }

  public String move(Player p, List<String> command) {
    if (over) {
      throw new IllegalStateException("Combat is over, so no one may move");
    }
    if (!movingTeam.isMember(p)) {
      throw new IllegalArgumentException("Player " + p.getName() +
                                         " is not on the moving team " +
                                         movingTeam.getName());
    }
    if (!yetToMove.contains(p)) {
      throw new IllegalArgumentException("Player " + p.getName() +
                                         " has already moved");
    }
    Stats pStats = movingTeam.getMemberStats(p);
    if (pStats.isOut()) {
      throw new IllegalArgumentException("Player " + p.getName() +
                                         " is knocked out");
    }

    // Turn command into input
    // - maneuver/m ad skill
    // - strike/a sd player
    // - skill/s sd skill [target]
    // - catch/c breath
    if (command.size() < 1) {
      throw new IllegalArgumentException("No command given");
    }
    Rules.Input input;
    Skill skill = null;
    String dName = null;
    Player dPlayer = null;
    Team dTeam = null;
    Stats dStats = null;
    switch (command.get(0)) {
      case "maneuver":
      case "m":
        if (command.size() != 3) {
          throw new IllegalArgumentException("Syntax: m[aneuver] <action dice> <skill name>");
        }
        skill = Skills.forName(command.get(2));
        input = new ManeuverInput(pStats, skill, Integer.parseInt(command.get(1)));
        break;
      case "attack":
      case "strike":
      case "a":
        if (command.size() != 3) {
          throw new IllegalArgumentException("Syntax: a[ttack] <strike dice> <target name>");
        }
        dName = command.get(2);
        dTeam = findDefenderTeam(dName);
        dPlayer = dTeam.getMemberByName(dName).get();
        dStats = dTeam.getMemberStats(dPlayer);
        input = new StrikeInput(pStats, Integer.parseInt(command.get(1)), dStats);
        break;
      case "skill":
      case "s":
        if (command.size() != 3 && command.size() != 4) {
          throw new IllegalArgumentException("Syntax: s[kill] <strike dice> <skill name> [<target name>]");
        }
        skill = Skills.forName(command.get(2));
        if (command.size() == 4) {
          if (skill.targetsSelf()) {
            throw new IllegalArgumentException("The chosen skill does not require a target");
          }
          dName = command.get(3);
          dTeam = findDefenderTeam(dName);
          dPlayer = dTeam.getMemberByName(dName).get();
          dStats = dTeam.getMemberStats(dPlayer);
        } else if (!skill.targetsSelf()) {
          throw new IllegalArgumentException("The chosen skill requires a target");
        }
        input = new SkillActionInput(pStats, Integer.parseInt(command.get(1)), skill, dStats);
        break;
      case "catch":
      case "c":
        input = new CatchBreathInput(pStats);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized command " +
                                           command.get(0));
    }

    // Run input through rules
    // In later Java versions, use switch with pattern matching
    Output output;
    if (input instanceof ManeuverInput) {
      output = rules.maneuver((ManeuverInput) input);
    } else if (input instanceof StrikeInput) {
      output = rules.strike((StrikeInput) input);
    } else if (input instanceof SkillActionInput) {
      output = rules.skill((SkillActionInput) input);
    } else if (input instanceof CatchBreathInput) {
      output = rules.catchBreath((CatchBreathInput) input);
    } else {
      throw new UnsupportedOperationException("Move with input " +
                                              input.getClass() + " missing!");
    }
    String commandResult = output.formatResult();

    // Replace stats if needed
    if (output instanceof SkillActionOutput) {
      SkillActionOutput saOutput = (SkillActionOutput) output;
      if (saOutput.newStats != null) {
        movingTeam.setMemberStats(p, saOutput.newStats);
      }
      if (saOutput.newDStats != null) {
        dTeam.setMemberStats(dPlayer, saOutput.newDStats);
      }
    }

    yetToMove.remove(p);

    checkIfOver();
    if (over) {
      // send a message that the team won, or something
      return new MoveResult(commandResult, null, null, true, winningTeam)
          .toString();
    }

    boolean newTeam = false;
    boolean newRound = false;
    if (yetToMove.isEmpty() || remainingOut(movingTeam, yetToMove)) {
      newTeam = true;
      movingTeamIndex++;
      if (movingTeamIndex >= teams.size()) {
        newRound = true;
        movingTeamIndex = 0;
        round++;
      }
      movingTeam = teams.get(movingTeamIndex);
      yetToMove = new HashSet<>(movingTeam.getMembers());
    }

    return new MoveResult(commandResult,
                          newTeam ? movingTeam.getName() : null,
                          newRound ? Integer.valueOf(round) : null,
                          false, null).toString();
  }

  public String resolveRound() {
    return null;
  }

  private void checkIfOver() {
    Team survivingTeam = null;
    for (Team team : teams) {
      if (team.isOut()) {
        continue;
      }
      if (survivingTeam == null) {
        survivingTeam = team;
      } else {
        return;
      }
    }

    over = true;
    winningTeam = survivingTeam;
  }

  public static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private List<Team> teams;
    private Rules rules;

    private Builder() {
      teams = new ArrayList<>();
      rules = new Rules();
    }

    Builder addTeam(Team team) {
      teams.add(team);
      return this;
    }

    Builder rules(Rules rules) {
      this.rules = Objects.requireNonNull(rules);
      return this;
    }

    Engine build() {
      Preconditions.checkState(teams.size() >= 2,
                               "At least two teams are required");
      return new Engine(teams, rules);
    }

    public String status() {
      if (teams.isEmpty()) {
        return "No teams have been added yet.";
      } else {
        StringBuilder b = new StringBuilder("Teams so far:\n");
        for (Team t : teams) {
          b.append("- ").append(t.getName()).append(":\n");
          b.append("  ");
          b.append(t.getMembers().stream()
                   .map(p -> p.getName())
                   .collect(Collectors.joining(", ")));
          b.append("\n");
        }
        return b.toString();
      }
    }
  }

  /*
   * These are the names of attributes players must have to join a team and
   * participate in combat.
   */

  static final String ATTR_NAME_SKILL_4 = "grapple_skill4";
  static final String ATTR_NAME_SKILL_3 = "grapple_skill3";
  static final String ATTR_NAME_SKILL_2 = "grapple_skill2";
  static final String ATTR_NAME_AP_MAX_SIZE = "grapple_apMaxSize";
  static final String ATTR_NAME_AD = "grapple_ad";
  static final String ATTR_NAME_SD = "grapple_sd";
  static final String ATTR_NAME_DEFENSE = "grapple_defense";
  static final String ATTR_NAME_MAX_WOUNDS = "grapple_maxWounds";

  private static Stats buildStats(Player player) {
    LOG.debug("Building stats for {}", player.getName());
    BaseStats stats = BaseStats.builder()
        .skill(Skills.forName(getAttr(player, ATTR_NAME_SKILL_4).getValue()), 4)
        .skill(Skills.forName(getAttr(player, ATTR_NAME_SKILL_3).getValue()), 3)
        .skill(Skills.forName(getAttr(player, ATTR_NAME_SKILL_2).getValue()), 2)
        .apMaxSize(getAttr(player, ATTR_NAME_AP_MAX_SIZE).getIntValue())
        .defense(getAttr(player, ATTR_NAME_DEFENSE).getIntValue())
        .maxWounds(getAttr(player, ATTR_NAME_MAX_WOUNDS).getIntValue())
        .build();
    stats.init(getAttr(player, ATTR_NAME_AD).getIntValue(),
               getAttr(player, ATTR_NAME_SD).getIntValue());
    return stats;
  }

  private static Attr getAttr(Player player, String name) {
    Optional<Attr> attrOpt = player.getAttr(name);
    if (attrOpt.isEmpty()) {
      throw new IllegalArgumentException("Player " + player.getName() +
                                         " is missing attribute " + name);
    }
    return attrOpt.get();
  }

  private static void drainStats(Player player, Stats stats) {
    int finalAd = stats.getAd() + (stats.getSd() + 1) / 2;
    int apMaxSize = getAttr(player, ATTR_NAME_AP_MAX_SIZE).getIntValue();
    if (finalAd > apMaxSize) {
      finalAd = apMaxSize;
    }
    player.setAttr(ATTR_NAME_AD, finalAd);
    player.setAttr(ATTR_NAME_SD, 0);
  }

  private Team findDefenderTeam(String defenderName) {
    for (Team t : teams) {
      if (t.equals(movingTeam)) {
        continue;
      }
      if (t.getMemberByName(defenderName).isPresent()) {
        return t;
      }
    }
    throw new IllegalArgumentException("No player named " + defenderName +
                                       " found on any team");
  }

  private static boolean remainingOut(Team team, Set<Player> players) {
    return players.stream()
        .map(p -> team.getMemberStats(p))
        .allMatch(s -> s.isOut());
  }
}
