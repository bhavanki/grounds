package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Saves the universe to a file.<p>
 *
 * Arguments: file to save to<br>
 * Checks: player is GOD
 */
public class SaveCommand extends Command<Boolean> {

  private final File f;

  public SaveCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.equals(Player.GOD)) {
      throw new CommandException("Only GOD can save the universe");
    }
    try {
      Universe.save(Universe.getCurrent(), f);
      Universe.setCurrentFile(f);
      actor.sendMessage(newInfoMessage("Saved universe to " + f.getName()));
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      throw new CommandException("Failed to save universe: " + e.getMessage());
    }
  }

  public static SaveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new SaveCommand(actor, player, new File(commandArgs.get(0)));
  }
}
