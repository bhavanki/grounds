package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Gets an attribute on a thing.<p>
 *
 * Arguments: name or ID of thing, attribute name<br>
 * Checks: player passes READ for thing
 */
public class GetAttrCommand extends Command<String> {

  private final Thing thing;
  private final String attrName;

  public GetAttrCommand(Actor actor, Player player, Thing thing, String attrName) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attrName = Objects.requireNonNull(attrName);
  }

  @Override
  public String execute() throws CommandException {
    checkPermission(Category.READ, thing, "You are not permitted to get attributes on this");
    Optional<Attr> attr = thing.getAttr(attrName);
    if (attr.isEmpty()) {
      throw new CommandException("There is no attribute named " + attrName + " on this");
    }
    return attr.get().toAttrSpec();
  }

  public static GetAttrCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing getThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new GetAttrCommand(actor, player, getThing, commandArgs.get(1));
  }
}
