package xyz.deszaras.grounds.command;

import java.util.Objects;
import xyz.deszaras.grounds.model.Attr;
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
}
