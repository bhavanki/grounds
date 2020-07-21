package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Loads a universe from a file.<p>
 *
 * Arguments: file to load
 */
@PermittedRoles(roles = {})
public class LoadCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(LoadCommand.class);

  private final File f;

  public LoadCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    // TBD prohibit if any players besides GOD are in use, especially
    // because they are disconnected from their players
    try {
      Universe loadedUniverse = Universe.load(f);

      Player currentGod = Universe.getCurrent() != null ?
          Universe.getCurrent().getThing(Player.GOD.getId(), Player.class)
              .orElse(Player.GOD) :
          Player.GOD;

      Optional<Player> newGod =
          loadedUniverse.getThing(Player.GOD.getId(), Player.class);
      if (newGod.isPresent()) {
        loadedUniverse.removeThing(newGod.get());
        loadedUniverse.addThing(currentGod);
      }

      loadedUniverse.removeGuests();

      Universe.setCurrent(loadedUniverse);
      Universe.setCurrentFile(f);

      if (newGod.isPresent()) {
        try {
          Optional<Thing> godLocation = newGod.get().getLocation();
          if (godLocation.isPresent()) {
            currentGod.setLocation(godLocation.get());
            godLocation.get().take(newGod.get());
            godLocation.get().give(currentGod);
          }
        } catch (MissingThingException e) {
          LOG.warn("Failed to move GOD to saved location in new universe", e);
        }
        // FUTURE: save new GOD's possessions?
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
