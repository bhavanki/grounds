package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Removes an attribute on a thing.<p>
 *
 * Arguments: name or ID of thing, attribute name<br>
 * Checks: player passes WRITE for thing
 */
public class RemoveAttrCommand extends Command {

  private final Thing thing;
  private final String attrName;

  public RemoveAttrCommand(Actor actor, Player player, Thing thing, String attrName) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attrName = Objects.requireNonNull(attrName);
  }

  @Override
  public boolean execute() {
    if (AttrNames.ALL_NAMES.contains(attrName) &&
        !player.equals(Player.GOD)) {
      actor.sendMessage("Only GOD may remove that attribute directly");
      return false;
    }

    if (!thing.passes(Category.WRITE, player)) {
      actor.sendMessage("You are not permitted to set attributes on this");
      return false;
    }
    thing.removeAttr(attrName);
    return true;
  }

  public static RemoveAttrCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing removeThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    String attrName = commandArgs.get(1);
    return new RemoveAttrCommand(actor, player, removeThing, attrName);
  }
}
