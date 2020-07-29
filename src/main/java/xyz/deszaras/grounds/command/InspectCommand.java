package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Inspects a thing, or just one attribute of a thing.<p>
 *
 * Arguments: name or ID of thing, optional attribute name<br>
 * Checks: player passes WRITE for thing (intentionally not just READ)
 */
@PermittedRoles(roles = { Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class InspectCommand extends Command<String> {

  private final Thing thing;
  private final String attrName;

  public InspectCommand(Actor actor, Player player, Thing thing, String attrName) {
    super(actor, player);
    this.thing = thing;
    this.attrName = attrName;
  }

  @Override
  protected String executeImpl() throws CommandException {
    checkPermission(Category.WRITE, thing, "You are not permitted to inspect this");
    if (attrName == null) {
      return thing.toJson();
    }
    Optional<Attr> attr = thing.getAttr(attrName);
    if (attr.isPresent()) {
      return attr.get().toJson();
    }
    throw new CommandException("Attribute " + attrName + " not present");
  }

  public static InspectCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing thing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    String attrName;
    if (commandArgs.size() > 1) {
      attrName = commandArgs.get(1);
    } else {
      attrName = null;
    }
    return new InspectCommand(actor, player, thing, attrName);
  }
}
