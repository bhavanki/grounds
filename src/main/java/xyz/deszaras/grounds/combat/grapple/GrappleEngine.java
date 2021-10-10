package xyz.deszaras.grounds.combat.grapple;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import xyz.deszaras.grounds.combat.Engine;
import xyz.deszaras.grounds.combat.Npc;
import xyz.deszaras.grounds.combat.Team;
import xyz.deszaras.grounds.combat.grapple.Rules.CatchBreathInput;
// import xyz.deszaras.grounds.combat.grapple.Rules.CatchBreathOutput;
// import xyz.deszaras.grounds.combat.grapple.Rules.Input;
import xyz.deszaras.grounds.combat.grapple.Rules.ManeuverInput;
// import xyz.deszaras.grounds.combat.grapple.Rules.ManeuverOutput;
import xyz.deszaras.grounds.combat.grapple.Rules.Output;
import xyz.deszaras.grounds.combat.grapple.Rules.SkillActionInput;
import xyz.deszaras.grounds.combat.grapple.Rules.SkillActionOutput;
import xyz.deszaras.grounds.combat.grapple.Rules.StrikeInput;
// import xyz.deszaras.grounds.combat.grapple.Rules.StrikeOutput;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;
import xyz.deszaras.grounds.util.LineOutput;
import xyz.deszaras.grounds.util.TabularOutput;

public class GrappleEngine extends Engine {

  private static final Logger LOG = LoggerFactory.getLogger(Engine.class);

  public static final int ROUND_NOT_STARTED = 0;

  protected static class MoveResult {
    private final String commandResult;
    private final String newMovingTeamName;
    private final Integer newRound;
    private final boolean over;
    private final GrappleTeam winningTeam;

    private MoveResult(String commandResult, String newMovingTeamName,
                       Integer newRound, boolean over, GrappleTeam winningTeam) {
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

  private final List<GrappleTeam> grappleTeams;
  private final Rules rules;

  private int round;
  private int movingTeamIndex;
  private GrappleTeam movingTeam;
  private Set<Player> yetToMove;
  private boolean over;
  private GrappleTeam winningTeam;

  private GrappleEngine(List<GrappleTeam> grappleTeams, Rules rules, int round,
                        int movingTeamIndex, Set<Player> yetToMove,
                        boolean over, GrappleTeam winningTeam) {
    super(grappleTeams);
    this.grappleTeams = ImmutableList.copyOf(grappleTeams);
    this.rules = rules;

    this.round = round;
    this.movingTeamIndex = movingTeamIndex;
    this.movingTeam = grappleTeams.get(movingTeamIndex);
    this.yetToMove = new HashSet<>(yetToMove);
    this.over = over;
    this.winningTeam = winningTeam;
  }

  @Override
  public void start() {
    Preconditions.checkState(round == ROUND_NOT_STARTED,
                             "Combat has already started");
    round = 1;
    movingTeamIndex = 0;
    movingTeam = grappleTeams.get(movingTeamIndex);
    yetToMove = new HashSet<>(movingTeam.getMembers());
    over = false;
    winningTeam = null;
  }

  @Override
  public void end() {
    for (GrappleTeam team : grappleTeams) {
      for (Player player : team.getMembers()) {
        Stats playerStats = team.getMemberStats(player);
        drainStats(player, playerStats);
      }
    }
  }

  private static final int STATUS_WIDTH = 37;

  @Override
  public String status() {
    String border = new LineOutput(STATUS_WIDTH, "=", Ansi.Color.MAGENTA, true).toString();
    String teamBorder = new LineOutput(STATUS_WIDTH, "- ", Ansi.Color.MAGENTA, true).toString();
    StringBuilder b = new StringBuilder(border).append("\n");

    if (over) {
      b.append(AnsiUtils.color("COMBAT IS OVER! Winning team: " +
                               (winningTeam != null ? winningTeam.getName() : "none!"),
                               Ansi.Color.RED, true));
    } else {
      b.append("Round: ").append(round);
    }
    for (GrappleTeam team : grappleTeams) {
      b.append("\n").append(teamBorder);
      String teamName = team.getName();
      boolean isMovingTeam = team.equals(movingTeam);
      if (isMovingTeam) {
        teamName += " <-";
      }
      TabularOutput table = new TabularOutput();
      table.defineColumn(teamName, "%-20.20s")
          .defineColumn("S4", "%2s")
          .defineColumn("S3", "%2s")
          .defineColumn("S2", "%2s")
          .defineColumn("AD/AP", "%5s")
          .defineColumn("SD", "%2s")
          .defineColumn("DEF", "%3s")
          .defineColumn("WOUNDS", "%6s");

      List<Player> memberList = new ArrayList<>(team.getMembers());
      Collections.sort(memberList, (p1, p2) -> p1.getName().compareTo(p2.getName()));
      for (Player player : memberList) {
        Stats playerStats = team.getMemberStats(player);
        String playerName = player.getName();
        if (playerStats.isOut()) {
          playerName = AnsiUtils.color(playerName, Ansi.Color.RED, false);
        } else if (isMovingTeam && yetToMove.contains(player)) {
          playerName += " <-";
        }
        Map<Skill, Integer> skills = playerStats.getSkills();
        String skill4Abbrev = "";
        String skill3Abbrev = "";
        String skill2Abbrev = "";
        for (Map.Entry<Skill, Integer> e : skills.entrySet()) {
          String abbrev = e.getKey().getAbbrev();
          if (playerStats.isUsed(e.getKey())) {
            abbrev = abbrev.toUpperCase();
          }
          switch (e.getValue().intValue()) {
            case 4:
              skill4Abbrev = abbrev;
              break;
            case 3:
              skill3Abbrev = abbrev;
              break;
            case 2:
              skill2Abbrev = abbrev;
              break;
          }
        }
        String adString = Integer.toString(playerStats.getAd()) + "/" +
            Integer.toString(playerStats.getApMaxSize());
        String woundString;
        int numWounds = playerStats.getWounds();
        if (playerStats.isOut()) {
          woundString = AnsiUtils.color("X".repeat(numWounds), Ansi.Color.RED, false);
        } else {
          woundString = "*".repeat(numWounds);
        }
        table.addRow(playerName,
                     skill4Abbrev,
                     skill3Abbrev,
                     skill2Abbrev,
                     adString,
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

  @VisibleForTesting
  Set<Player> getYetToMove() {
    return ImmutableSet.copyOf(yetToMove);
  }

  @VisibleForTesting
  boolean isOver() {
    return over;
  }

  @VisibleForTesting
  Team getWinningTeam() {
    return winningTeam;
  }

  @Override
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
    GrappleTeam dTeam = null;
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
          // this probably should be in Rules
          if (dName.equals(p.getName())) {
            throw new IllegalArgumentException("The chosen skill cannot be targeted at yourself");
          }
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
      movingTeam = grappleTeams.get(movingTeamIndex);
      yetToMove = new HashSet<>(movingTeam.getMembers());
    }

    return new MoveResult(commandResult,
                          newTeam ? movingTeam.getName() : null,
                          newRound ? Integer.valueOf(round) : null,
                          false, null).toString();
  }

  @Override
  public String resolveRound() {
    return null;
  }

  private void checkIfOver() {
    GrappleTeam survivingTeam = null;
    for (GrappleTeam team : grappleTeams) {
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

  static class Builder extends Engine.Builder {
    private List<GrappleTeam> teams;
    private Rules rules;
    private int round;
    private int movingTeamIndex;
    private Set<Player> yetToMove;
    private boolean over;
    private GrappleTeam winningTeam;

    private Builder() {
      teams = new ArrayList<>();
      rules = new Rules();
      round = ROUND_NOT_STARTED;
      movingTeamIndex = 0;
      yetToMove = new HashSet<>();
      over = false;
      winningTeam = null;
    }

    @Override
    public Builder addTeam(Team team) {
      if (team == null || !(team instanceof GrappleTeam)) {
        throw new IllegalArgumentException("Team is not a Grapple team");
      }
      teams.add((GrappleTeam) team);
      return this;
    }

    Builder addTeams(Collection<GrappleTeam> teams) {
      this.teams.addAll(teams);
      return this;
    }

    Builder rules(Rules rules) {
      this.rules = Objects.requireNonNull(rules);
      return this;
    }

    Builder round(int round) {
      Preconditions.checkArgument(round > 0, "round must be positive");
      this.round = round;
      return this;
    }

    Builder movingTeamIndex(int movingTeamIndex) {
      Preconditions.checkArgument(movingTeamIndex >= 0, "movingTeamIndex must be non-negative");
      this.movingTeamIndex = movingTeamIndex;
      return this;
    }

    Builder addYetToMove(Player player) {
      yetToMove.add(Objects.requireNonNull(player, "player may not be null"));
      return this;
    }

    Builder addYetToMoves(Set<Player> players) {
      for (Player p : players) {
        yetToMove.add(p);
      }
      return this;
    }

    Builder over(boolean over) {
      this.over = over;
      return this;
    }

    Builder winningTeam(GrappleTeam winningTeam) {
      this.winningTeam = winningTeam;
      return this;
    }

    @Override
    public GrappleEngine build() {
      Preconditions.checkState(teams.size() >= 2,
                               "At least two teams are required");
      Preconditions.checkState(movingTeamIndex < teams.size(),
                               "Moving team index may not exceed " + (teams.size() - 1));
      GrappleTeam movingTeam = teams.get(movingTeamIndex);
      Preconditions.checkState(round == ROUND_NOT_STARTED || over || !yetToMove.isEmpty(),
                               "Since combat isn't over, at least one player must be yet to move");
      Preconditions.checkState(yetToMove.stream()
                               .allMatch(p -> movingTeam.isMember(p)),
                               "Some yet-to-move players are not on the moving team");
      Preconditions.checkState(winningTeam == null || over,
                               "A winning team may not be provided when combat isn't over");
      if (winningTeam != null) {
        Preconditions.checkState(teams.stream()
                                 .anyMatch(t -> t.getName().equals(winningTeam.getName())),
                                 "The winning team is not a listed team");
      }
      return new GrappleEngine(teams, rules, round, movingTeamIndex, yetToMove,
                               over, winningTeam);
    }

    @Override
    public String status() {
      if (teams.isEmpty()) {
        return "No teams have been added yet.";
      } else {
        StringBuilder b = new StringBuilder("Teams so far:\n");
        for (GrappleTeam t : teams) {
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

  static Stats buildStats(Player player) {
    LOG.debug("Building stats for {}", player.getName());
    BaseStats.Builder statsBuilder = BaseStats.builder()
        .apMaxSize(getRequiredAttr(player, ATTR_NAME_AP_MAX_SIZE).getIntValue())
        .defense(getRequiredAttr(player, ATTR_NAME_DEFENSE).getIntValue())
        .maxWounds(getRequiredAttr(player, ATTR_NAME_MAX_WOUNDS).getIntValue());
    Optional<Attr> skill4Attr = getAttr(player, ATTR_NAME_SKILL_4);
    if (skill4Attr.isPresent()) {
      statsBuilder.skill(Skills.forName(skill4Attr.get().getValue()), 4);
    }
    Optional<Attr> skill3Attr = getAttr(player, ATTR_NAME_SKILL_3);
    if (skill3Attr.isPresent()) {
      statsBuilder.skill(Skills.forName(skill3Attr.get().getValue()), 3);
    }
    Optional<Attr> skill2Attr = getAttr(player, ATTR_NAME_SKILL_2);
    if (skill2Attr.isPresent()) {
      statsBuilder.skill(Skills.forName(skill2Attr.get().getValue()), 2);
    }
    if (player instanceof Npc) {
      statsBuilder.npc();
    }
    BaseStats stats = statsBuilder.build();
    stats.init(getRequiredAttr(player, ATTR_NAME_AD).getIntValue(),
               getRequiredAttr(player, ATTR_NAME_SD).getIntValue());
    return stats;
  }

  private static Optional<Attr> getAttr(Player player, String name) {
    Optional<Attr> attrOpt = player.getAttr(name);
    if (attrOpt.isEmpty() && !(player instanceof Npc)) {
      throw new IllegalArgumentException("Player " + player.getName() +
                                         " is missing attribute " + name);
    }
    return attrOpt;
  }

  private static Attr getRequiredAttr(Player player, String name) {
    Optional<Attr> attrOpt = player.getAttr(name);
    if (attrOpt.isEmpty()) {
      throw new IllegalArgumentException("Player " + player.getName() +
                                         " is missing attribute " + name);
    }
    return attrOpt.get();
  }

  private static void drainStats(Player player, Stats stats) {
    int finalAd = stats.getAd() + (stats.getSd() + 1) / 2;
    int apMaxSize = getRequiredAttr(player, ATTR_NAME_AP_MAX_SIZE).getIntValue();
    if (finalAd > apMaxSize) {
      finalAd = apMaxSize;
    }
    player.setAttr(ATTR_NAME_AD, finalAd);
    player.setAttr(ATTR_NAME_SD, 0);
  }

  private GrappleTeam findDefenderTeam(String defenderName) {
    for (GrappleTeam t : grappleTeams) {
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

  private static boolean remainingOut(GrappleTeam team, Set<Player> players) {
    return players.stream()
        .map(p -> team.getMemberStats(p))
        .allMatch(s -> s.isOut());
  }

  public ProtoModel.Engine toProto() {
    return ProtoModel.Engine.newBuilder()
        .addAllTeams(grappleTeams.stream()
                     .map(GrappleTeam::toProto)
                     .collect(Collectors.toList()))
        .setRound(round)
        .setMovingTeamIndex(movingTeamIndex)
        .addAllYetToMove(yetToMove.stream()
                         .map(Player::getName)
                         .collect(Collectors.toList()))
        .setOver(over)
        .setWinningTeamName(winningTeam != null ? winningTeam.getName() : "")
        .build();
  }

  public static GrappleEngine fromProto(ProtoModel.Engine proto) {
    Universe universe = Universe.getCurrent();

    List<GrappleTeam> teams = proto.getTeamsList().stream()
        .map(teamProto -> GrappleTeam.fromProto(teamProto))
        .collect(Collectors.toList());

    Set<Player> yetToMove = proto.getYetToMoveList().stream()
        .map(playerName -> universe.getThingByName(playerName, Player.class)
                .orElseThrow(() -> new IllegalArgumentException("Player " + playerName + " not found")))
        .collect(Collectors.toSet());

    String winningTeamName = proto.getWinningTeamName();
    GrappleTeam winningTeam = null;
    if (!winningTeamName.isEmpty()) {
      winningTeam = teams.stream()
          .filter(t -> t.getName().equals(winningTeamName))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("The winning team is not a listed team"));
    }

    return GrappleEngine.builder()
        .addTeams(teams)
        .round(proto.getRound())
        .movingTeamIndex(proto.getMovingTeamIndex())
        .addYetToMoves(yetToMove)
        .over(proto.getOver())
        .winningTeam(winningTeam)
        .build();
  }
}
