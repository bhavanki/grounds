package xyz.deszaras.grounds.server;

import com.google.common.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.jline.reader.LineReader;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;

/**
 * An interaction for selecting a player to play as.
 */
class PlayerSelection implements Callable<Player> {

  @VisibleForTesting
  static final String AUTO_SELECTING_FORMAT = "Auto-selecting initial player %s\n";
  @VisibleForTesting
  static final String INVALID_PLAYER_NAME_FORMAT = "Invalid player name %s\n\n";
  @VisibleForTesting
  static final String INVALID_PLAYER_NUMBER_FORMAT = "Invalid player number %d\n\n";
  @VisibleForTesting
  static final String OCCUPIED_FORMAT = "Someone is currently playing as %s.\n\n";
  @VisibleForTesting
  static final String PERMITTED_PLAYERS = "Permitted players:";
  @VisibleForTesting
  static final String SELECTING_FORMAT = "Selecting player %s\n";
  @VisibleForTesting
  static final String SELECTION_PROMPT = "Select your initial player (exit to disconnect): ";

  private final LineReader lineReader;
  private final PrintWriter out;
  private final Actor actor;
  private final Universe universe;

  /**
   * Creates a new interaction.
   *
   * @param lineReader line reader for accepting input
   * @param out        print writer for producing output
   * @param actor      actor selecting a player
   * @param universe   universe where players reside
   */
  PlayerSelection(LineReader lineReader, PrintWriter out, Actor actor, Universe universe) {
    this.lineReader = lineReader;
    this.out = out;
    this.actor = actor;
    this.universe = universe;
  }

  /**
   * Selects a player.
   *
   * @return selected player
   * @throws IOException   [description]
   */
  @Override
  public Player call() {
    List<Player> permittedPlayers =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername())
        .get().getPlayers().stream()
        .map(id -> universe.getThing(id, Player.class))
        .filter(p -> p.isPresent())
        .map(p -> p.get())
        .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
        .collect(Collectors.toList());
    out.println(PERMITTED_PLAYERS);
    for (int i = 1; i <= permittedPlayers.size(); i++) {
      out.printf("  %d. %s\n", i, permittedPlayers.get(i - 1).getName());
    }
    out.println("");

    Player chosenPlayer = null;
    if (permittedPlayers.size() == 1) {
      chosenPlayer = permittedPlayers.get(0);
      out.printf(AUTO_SELECTING_FORMAT, chosenPlayer.getName());

      if (!chosenPlayer.trySetCurrentActor(actor)) {
        String occupiedMessage = String.format(OCCUPIED_FORMAT, chosenPlayer.getName());
        out.print(AnsiUtils.color(occupiedMessage, Ansi.Color.YELLOW, false));
        chosenPlayer = null;
      }
    }
    while (chosenPlayer == null) {
      String line = lineReader.readLine(SELECTION_PROMPT);
      if (line == null) {
        return null;
      }
      if (line.equals("exit")) {
        return null;
      }
      if (line.isEmpty()) {
        continue;
      }

      boolean numberEntered;
      int spNum;
      try {
        spNum = Integer.parseInt(line);
        numberEntered = true;
      } catch (NumberFormatException e) {
        spNum = 0;
        numberEntered = false;
      }
      Player sp = null;
      if (numberEntered) {
        if (spNum >= 1 && spNum <= permittedPlayers.size()) {
          sp = permittedPlayers.get(spNum - 1);
        } else {
          String invalidNMessage = String.format(INVALID_PLAYER_NUMBER_FORMAT, spNum);
          out.print(AnsiUtils.color(invalidNMessage, Ansi.Color.RED, false));
        }
      } else {
        for (Player p : permittedPlayers) {
          if (p.getName().startsWith(line)) {
            sp = p;
            break;
          }
        }
        if (sp == null) {
          String invalidMessage = String.format(INVALID_PLAYER_NAME_FORMAT, line);
          out.print(AnsiUtils.color(invalidMessage, Ansi.Color.RED, false));
        }
      }

      if (sp != null) {
        out.printf(SELECTING_FORMAT, sp.getName());
        if (sp.trySetCurrentActor(actor)) {
          chosenPlayer = sp;
        } else {
          String occupiedMessage = String.format(OCCUPIED_FORMAT, sp.getName());
          out.print(AnsiUtils.color(occupiedMessage, Ansi.Color.RED, false));
        }
      }
    }

    return chosenPlayer;
  }

}
