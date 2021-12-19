package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Sets an attribute on a thing.<p>
 *
 * Arguments: name or ID of thing, attribute spec
 *
 * For THING type attributes, this command will resolve a thing name in an
 * attribute spec into an ID. If resolution fails, the attribute is not set.
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class SetAttrCommand extends Command<Boolean> {

  private final Thing thing;
  private final Attr attr;

  public SetAttrCommand(Actor actor, Player player, Thing thing, Attr attr) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attr = Objects.requireNonNull(attr);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (AttrNames.PROTECTED.contains(attr.getName()) &&
        !player.equals(Player.GOD)) {
      throw new CommandException("Only GOD may set that attribute directly");
    }

    checkPermission(Category.WRITE, thing, "You are not permitted to set attributes on this");
    thing.setAttr(attr);
    return true;
  }

  public static SetAttrCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing setThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    try {
      Attr attr = Attr.fromAttrSpec(commandArgs.get(1));
      if (attr.getType() == Attr.Type.THING) {
        Thing attrThing = CommandArgumentResolver.INSTANCE
            .resolve(attr.getValue(), Thing.class, player);
        attr = new Attr(attr.getName(), attrThing);
      }
      return new SetAttrCommand(actor, player, setThing, attr);
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Failed to build attr from spec |" + commandArgs.get(1) + "|: " + e.getMessage());
    }
  }
}
