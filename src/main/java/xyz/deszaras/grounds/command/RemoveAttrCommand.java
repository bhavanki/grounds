package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

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
    if (!thing.passes(Category.WRITE, player)) {
      actor.sendMessage("You are not permitted to set attributes on this");
      return false;
    }
    thing.removeAttr(attrName);
    return true;
  }

  public static RemoveAttrCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 2);
    Optional<Thing> setThing = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
    if (!setThing.isPresent()) {
      throw new CommandException("Failed to find thing in universe");
    }
    String attrName = commandArgs.get(1);
    return new RemoveAttrCommand(actor, player, setThing.get(), attrName);
  }
}
