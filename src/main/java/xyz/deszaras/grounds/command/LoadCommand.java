package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Loads a universe from a file.<p>
 *
 * Arguments: file to load<br>
 * Checks: player is GOD
 */
public class LoadCommand extends Command<Boolean> {

  private final File f;

  public LoadCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.equals(Player.GOD)) {
      throw new CommandException("Only GOD can load a universe");
    }
    // TBD prohibit if any players besides GOD are in use, especially
    // because they are disconnected from their players
    try {
      Universe.setCurrent(Universe.load(f));
      actor.sendMessage(newInfoMessage("Loaded universe from " + f.getName()));
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      throw new CommandException("Failed to load universe: " + e.getMessage());
    }
  }

  public static LoadCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new LoadCommand(actor, player, new File(commandArgs.get(0)));
  }
}
