package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Loads a universe from a file.<p>
 *
 * Arguments: file to load
 */
@PermittedRoles(roles = {})
public class LoadCommand extends Command<Boolean> {

  private final File f;

  public LoadCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    // TBD prohibit if any players besides GOD are in use, especially
    // because they are disconnected from their players
    Optional<Player> previousUniverseGodPlayer =
        Universe.getCurrent().getThing(Player.GOD.getId(), Player.class);
    try {
      Universe loadedUniverse = Universe.load(f);
      Universe.setCurrent(loadedUniverse);
      Universe.setCurrentFile(f);

      Optional<Player> newUniverseGodPlayer =
          loadedUniverse.getThing(Player.GOD.getId(), Player.class);
      if (newUniverseGodPlayer.isPresent() && previousUniverseGodPlayer.isPresent()) {
        newUniverseGodPlayer.get().setCurrentActor(previousUniverseGodPlayer.get().getCurrentActor().orElse(null));
      }

      player.sendMessage(newInfoMessage("Loaded universe from " + f.getName()));
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
