package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Inspects a thing.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player passes WRITE for thing (intentionally not just READ)
 */
public class InspectCommand extends Command {

  private final Thing thing;

  public InspectCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = thing;
  }

  @Override
  public boolean execute() {
    if (!thing.passes(Category.WRITE, player)) {
      actor.sendMessage("You are not permitted to inspect this");
      return false;
    }
    actor.sendMessage(thing.toJson());
    return true;
  }

  public static InspectCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing thing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new InspectCommand(actor, player, thing);
  }
}
