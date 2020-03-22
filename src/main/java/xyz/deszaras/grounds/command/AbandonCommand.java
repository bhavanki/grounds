package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Revokes ownership of a thing from a player.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player owns thing
 */
public class AbandonCommand extends Command {

  private final Thing thing;

  public AbandonCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public boolean execute() {
    if (!thing.getOwner().equals(player)) {
      actor.sendMessage("You do not own that");
      return false;
    }
    return new RemoveAttrCommand(actor, player, thing, AttrNames.OWNER).execute();
  }

  public static AbandonCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing abandonedThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new AbandonCommand(actor, player, abandonedThing);
  }
}
