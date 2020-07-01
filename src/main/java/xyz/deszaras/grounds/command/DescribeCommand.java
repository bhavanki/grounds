package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Describes a thing, or sets its description.<p>
 *
 * Arguments: name or ID of thing, optional description<br>
 * Checks: player passes READ for thing to see description, WRITE to set it
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class DescribeCommand extends Command<String> {

  public static final String NO_DESCRIPTION = "-";

  private final Thing thing;
  private final String newDescription;

  public DescribeCommand(Actor actor, Player player, Thing thing,
                         String newDescription) {
    super(actor, player);
    this.thing = thing;
    this.newDescription = newDescription;
  }

  @Override
  protected String executeImpl() throws CommandException {
    if (newDescription != null) {
      checkPermission(Category.WRITE, thing, "You are not permitted to set this thing's description");
      if (NO_DESCRIPTION.equals(newDescription)) {
        thing.setDescription(null);
      } else {
        thing.setDescription(newDescription);
      }
    } else {
      checkPermission(Category.READ, thing, "You are not permitted to see this thing's description");
    }
    return thing.getDescription().orElse("No description available");
  }

  public static DescribeCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing thing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    String newDescription = commandArgs.size() > 1 ? commandArgs.get(1) : null;
    return new DescribeCommand(actor, player, thing, newDescription);
  }
}
