package xyz.deszaras.grounds.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Shows a listing of all of the things in the universe.<p>
 *
 * Checks: player is wizard in universe
 */
public class IndexCommand extends Command<String> {

  public IndexCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public String execute() throws CommandException {
    checkIfWizard("You are not a wizard, so you may not index it");

    List<Thing> things = new ArrayList<>(Universe.getCurrent().getThings());
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

    StringBuilder b = new StringBuilder();
    b.append(String.format("%12.12s %25.25s %s\n", "TYPE", "NAME", "ID"));
    b.append(String.format("%12.12s %25.25s %s\n", "----", "----", "--"));
    for (Thing t : things) {
      b.append(String.format("%12.12s %25.25s %s\n", t.getClass().getSimpleName(),
                             t.getName(), t.getId().toString()));
    }
    return b.toString();
  }

  public static IndexCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    return new IndexCommand(actor, player);
  }
}
