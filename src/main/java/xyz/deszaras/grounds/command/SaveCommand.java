package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;

/**
 * Saves the multiverse to a file.<p>
 *
 * Arguments: file to save to<br>
 * Checks: player is GOD
 */
public class SaveCommand extends Command {

  private final File f;

  public SaveCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  public boolean execute() {
    if (!player.equals(Player.GOD)) {
      actor.sendMessage("Only GOD can save the multiverse");
      return false;
    }
    try {
      Multiverse.save(f);
      actor.sendMessage("Saved multiverse to " + f.getName());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      actor.sendMessage("Failed to save multiverse: " + e.getMessage());
    }
    return false;
  }

  public static SaveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new SaveCommand(actor, player, new File(commandArgs.get(0)));
  }
}
