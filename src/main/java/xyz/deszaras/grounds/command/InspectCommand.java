package xyz.deszaras.grounds.command;

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
    actor.sendMessage(thing.toJson());
    return true;
  }

}
