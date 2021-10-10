package xyz.deszaras.grounds.combat.grapple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.combat.Team;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * A team in the Grapple combat system.
 */
public class GrappleTeam extends Team {

  private final Map<Player, Stats> members;

  private GrappleTeam(String name, Map<Player, Stats> members) {
    super(name);
    this.members = new HashMap<>(members);
  }

  @Override
  public boolean isMember(Player player) {
    return members.containsKey(player);
  }

  @Override
  public Optional<Player> getMemberByName(String playerName) {
    return members.keySet().stream()
        .filter(p -> p.getName().equals(playerName))
        .findFirst();
  }

  @Override
  public Set<Player> getMembers() {
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
  static class Builder extends Team.Builder {
    private Map<Player, Stats> members;

    private Builder(String name) {
      super(name);
      members = new HashMap<>();
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
    @Override
    public Builder member(Player player) {
      return member(player, GrappleEngine.buildStats(player));
    }

    /**
     * Adds a new player to this team.
     *
     * @param  player player to add as member
     * @param  stats  player stats
     * @return        this builder
     * @throws IllegalArgumentException if the player is already a member of
     *         this team
     */
    public Builder member(Player player, Stats stats) {
      if (members.containsKey(player)) {
        throw new IllegalArgumentException(player.getName() + " is already a member of team " +
                                           name);
      }
      members.put(player, stats);
      return this;
    }

    /**
     * Removes a player from this team.
     *
     * @param  player player to remove as member
     * @return        this builder
     */
    @Override
    public Builder removeMember(Player player) {
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
    @Override
    public Builder removeMember(String playerName) {
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
    @Override
    public Set<Player> getMembers() {
      return members.keySet();
    }

    /**
     * Builds a new team.
     *
     * @return new team
     * @throws IllegalStateException if there are no members
     */
    @Override
    public GrappleTeam build() {
      Preconditions.checkState(members.size() >= 1,
                               "At least one team member is required");
      return new GrappleTeam(name, members);
    }

    /**
     * Returns the status of this team so far.
     *
     * @return team status
     */
    @Override
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

  public ProtoModel.Team toProto() {
    return ProtoModel.Team.newBuilder()
        .setName(getName())
        .putAllMembers(members.entrySet().stream()
                       .collect(Collectors.toMap(e -> e.getKey().getName(),
                                                 e -> e.getValue().toProto())))
        .addAllNpcs(members.keySet().stream()
                   .filter(p -> p instanceof GrappleNpc)
                   .map(p -> ((GrappleNpc) p).toProto())
                   .collect(Collectors.toList()))
        .build();
  }

  public static GrappleTeam fromProto(ProtoModel.Team proto) {
    Universe universe = Universe.getCurrent();
    GrappleTeam.Builder builder = GrappleTeam.builder(proto.getName());

    Map<String, GrappleNpc> npcMap = proto.getNpcsList().stream()
        .map(protoNpc -> GrappleNpc.fromProto(protoNpc))
        .collect(Collectors.toMap(GrappleNpc::getName, Function.identity()));

    for (Map.Entry<String, ProtoModel.Stats> e : proto.getMembersMap().entrySet()) {
      String playerName = e.getKey();
      Stats playerStats = Stats.fromProto(e.getValue());
      if (npcMap.containsKey(playerName)) {
        builder.member(npcMap.get(playerName), playerStats);
      } else {
        Optional<Player> player = universe.getThingByName(playerName, Player.class);
        if (player.isPresent()) {
          builder.member(player.get(), playerStats);
        } else {
          // Convert the player into an NPC.
          GrappleNpc newNpc = new GrappleNpc(playerName, playerStats);
          builder.member(newNpc, playerStats);
        }
      }
    }

    return builder.build();
  }
}
