package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Saves the universe to a file.<p>
 *
 * Arguments: file to save to
 */
@PermittedRoles(roles = {})
public class SaveCommand extends Command<Boolean> {

  private final File f;

  public SaveCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = f;
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    try {
      File savedFile;
      if (f != null) {
        Universe.save(Universe.getCurrent(), f);
        Universe.setCurrentFile(f);
        savedFile = f;
      } else {
        Universe.saveCurrent(true);
        savedFile = Universe.getCurrentFile();
      }
      player.sendMessage(newInfoMessage("Saved universe to " + savedFile.getName()));
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      throw new CommandException("Failed to save universe: " + e.getMessage());
    }
  }

  public static SaveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    return new SaveCommand(actor, player,
                           commandArgs.size() > 0 ?
                              new File(commandArgs.get(0)) : null);
  }
}
