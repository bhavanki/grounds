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
public class RemoveAttrCommand extends Command<Boolean> {

  private final Thing thing;
  private final String attrName;

  public RemoveAttrCommand(Actor actor, Player player, Thing thing, String attrName) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attrName = Objects.requireNonNull(attrName);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (AttrNames.ALL_NAMES.contains(attrName) &&
        !player.equals(Player.GOD)) {
      throw new CommandException("Only GOD may remove that attribute directly");
    }

    checkPermission(Category.WRITE, thing, "You are not permitted to remove attributes on this");
    thing.removeAttr(attrName);
    return true;
  }

  public static RemoveAttrCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing removeThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    String attrName = commandArgs.get(1);
    return new RemoveAttrCommand(actor, player, removeThing, attrName);
  }
}
