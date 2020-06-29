package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class DropCommand extends Command<Boolean> {

  private final Thing thing;

  public DropCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (!player.has(thing)) {
      throw new CommandException("You aren't holding that");
    }
    // This next check is questionable
    checkPermission(Category.GENERAL, thing, "You are unable to drop that");
    Place location = getPlayerLocation("drop anything");

    player.take(thing);
    location.give(thing);
    thing.setLocation(location);
    return true;
  }

  public static DropCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing droppedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new DropCommand(actor, player, droppedThing);
  }
}
