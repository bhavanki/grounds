package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class InspectCommand extends Command {

  private final Thing thing;

  public InspectCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = thing;
  }

  @Override
  public boolean execute() {
    if (!thing.passes(Category.WRITE, player)) { // WRITE is intentional
      actor.sendMessage("You are not permitted to inspect this");
      return false;
    }
    actor.sendMessage(thing.toJson());
    return true;
  }

  public static InspectCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    Optional<Thing> thing = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
    if (!thing.isPresent()) {
      throw new CommandException("Failed to find thing in universe");
    }
    return new InspectCommand(actor, player, thing.get());
  }
}
