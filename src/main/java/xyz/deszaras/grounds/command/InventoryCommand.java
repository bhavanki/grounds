package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.util.AnsiUtils;

public class InventoryCommand extends Command<String> {

  public InventoryCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public String execute() {
    StringBuilder b = new StringBuilder();
    player.getContents().forEach(id -> {
      Optional<Thing> t = Multiverse.MULTIVERSE.findThing(id);
      if (t.isPresent()) {
        b.append("- " + AnsiUtils.listing(t.get()) + "\n");
      }
    });
    return b.toString();
  }

  public static InventoryCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    return new InventoryCommand(actor, player);
  }
}
