package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Revokes ownership of a thing from a player.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player owns thing
 */
public class AbandonCommand extends Command<Boolean> {

  private final Thing thing;

  public AbandonCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.equals(thing.getOwner().orElse(null))) {
      throw new CommandException("You do not own that");
    }
    thing.setOwner(null);
    return true;
  }

  public static AbandonCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing abandonedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new AbandonCommand(actor, player, abandonedThing);
  }
}
