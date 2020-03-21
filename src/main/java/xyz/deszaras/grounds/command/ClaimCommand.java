package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class ClaimCommand extends Command {

  private final Thing thing;

  public ClaimCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public boolean execute() {
    Attr attr =
        Attr.fromAttrSpec(Attr.buildAttrSpec(AttrNames.OWNER,
                                             Attr.Type.STRING,
                                             player.getId().toString()));
    return new SetAttrCommand(actor, player, thing, attr).execute();
  }

  public static ClaimCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    Optional<Thing> setThing = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
    if (!setThing.isPresent()) {
      throw new CommandException("Failed to find thing in universe");
    }
    return new ClaimCommand(actor, player, setThing.get());
  }
}
