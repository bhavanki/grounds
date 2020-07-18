package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Gets the names of attributes on a thing.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player passes READ for thing
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE },
                failureMessage = "You are a guest, so you may not get attribute names")
public class GetAttrNamesCommand extends Command<String> {

  private final Thing thing;

  public GetAttrNamesCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  protected String executeImpl() throws CommandException {
    checkPermission(Category.READ, thing, "You are not permitted to get attribute name on this");
    return thing.getAttrs().stream()
        .map(a -> a.getName())
        .sorted()
        .collect(Collectors.joining(","));
  }

  public static GetAttrNamesCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing getThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new GetAttrNamesCommand(actor, player, getThing);
  }
}
