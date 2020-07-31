package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.AnsiUtils;

@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class InventoryCommand extends Command<String> {

  public InventoryCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() {
    boolean showId =
        Boolean.parseBoolean(actor.getPreference(Actor.PREFERENCE_SHOW_IDS).orElse("false"));

    StringBuilder b = new StringBuilder();
    player.getContents().forEach(id -> {
      Optional<Thing> t = Universe.getCurrent().getThing(id);
      if (t.isPresent()) {
        b.append("- " + AnsiUtils.listing(t.get(), showId) + "\n");
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
