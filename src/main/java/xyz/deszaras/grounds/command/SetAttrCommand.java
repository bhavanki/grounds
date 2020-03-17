package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class SetAttrCommand extends Command {

  private final Thing thing;
  private final Attr attr;

  public SetAttrCommand(Actor actor, Player player, Thing thing, Attr attr) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.attr = Objects.requireNonNull(attr);
  }

  @Override
  public boolean execute() {
    thing.setAttr(attr);
    return true;
  }

  public static SetAttrCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 2);
    Optional<Thing> setThing = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
    if (!setThing.isPresent()) {
      throw new CommandException("Failed to find thing in universe");
    }
    try {
      Attr attr = Attr.fromAttrSpec(commandArgs.get(1));
      return new SetAttrCommand(actor, player, setThing.get(), attr);
    } catch (IllegalArgumentException e) {
      throw new CommandException("Failed to build attr from spec |" + commandArgs.get(1) + "|: " + e.getMessage());
    }
  }
}
