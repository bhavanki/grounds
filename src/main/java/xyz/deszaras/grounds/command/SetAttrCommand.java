package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Sets an attribute on a thing.<p>
 *
 * Arguments: name or ID of thing, attribute spec<br>
 * Checks: player passes WRITE for thing
 */
public class SetAttrCommand extends Command {

  private final Thing thing;
  private final Attr attr;

  public SetAttrCommand(Actor actor, Player player, Thing thing, Attr attr) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attr = Objects.requireNonNull(attr);
  }

  @Override
  public boolean execute() {
    if (!thing.passes(Category.WRITE, player)) {
      actor.sendMessage("You are not permitted to set attributes on this");
      return false;
    }
    thing.setAttr(attr);
    return true;
  }

  public static SetAttrCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing setThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    try {
      Attr attr = Attr.fromAttrSpec(commandArgs.get(1));
      return new SetAttrCommand(actor, player, setThing, attr);
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Failed to build attr from spec |" + commandArgs.get(1) + "|: " + e.getMessage());
    }
  }
}
