package xyz.deszaras.grounds.combat;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.model.Player;

public abstract class Engine {

  protected final List<? extends Team> teams;

  protected Engine(List<? extends Team> teams) {
    this.teams = ImmutableList.copyOf(teams);
  }

  public List<Team> getTeams() {
    return ImmutableList.copyOf(teams);
  }

  public abstract void start();

  public abstract void end();

  public abstract String status();

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

  public abstract String move(Player p, List<String> command);

  public abstract String resolveRound();

  public abstract static class Builder {

    public abstract Builder addTeam(Team team);

    public abstract Engine build();

    public abstract String status();
  }
}
