package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class InventoryCommand extends Command {

  public InventoryCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public boolean execute() {
    StringBuilder b = new StringBuilder();
    player.getContents().forEach(id -> {
      Optional<Thing> t = Multiverse.MULTIVERSE.findThing(id);
      if (t.isPresent()) {
        b.append("- " + t.get().getName () + " [" + t.get().getId() + "]\n");
      }
    });
    actor.sendMessage(b.toString());
    return true;
  }

  public static InventoryCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    return new InventoryCommand(actor, player);
  }

  public static String help() {
    return "INVENTORY\n\n" +
        "Lists your inventory (contents)";
  }
}
