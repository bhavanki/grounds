package xyz.deszaras.grounds.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Shows a listing of all of the things in a universe.<p>
 *
 * Arguments: name of universe<br>
 * Checks: player is wizard in universe
 */
public class IndexCommand extends Command {

  private final Universe universe;

  public IndexCommand(Actor actor, Player player, Universe universe) {
    super(actor, player);
    this.universe = universe;
  }

  @Override
  public boolean execute() {
    if (!Role.isWizard(player, universe)) {
      actor.sendMessage("You are not a wizard in this universe, so you may not inspect");
      return false;
    }

    List<Thing> things = new ArrayList<>(universe.getThings());
    Collections.sort(things,
                     (t1, t2) -> {
                      int c = t1.getClass().getSimpleName().compareTo(t2.getClass().getSimpleName());
                      if (c != 0) {
                        return c;
                      }
                      c = t1.getName().compareTo(t2.getName());
                      if (c != 0) {
                        return c;
                      }
                      return t1.getId().compareTo(t2.getId());
                     });

    for (Thing t : things) {
      actor.sendMessage(String.format("%12.12s %25.25s %s", t.getClass().getSimpleName(),
                                      t.getName(), t.getId().toString()));
    }
    return true;
  }

  public static IndexCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String name = commandArgs.get(0);
    if (!Multiverse.MULTIVERSE.hasUniverse(name)) {
      throw new CommandFactoryException("Universe " + name + " does not exist");
    }
    return new IndexCommand(actor, player, Multiverse.MULTIVERSE.getUniverse(name));
  }
}