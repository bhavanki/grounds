package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import xyz.deszaras.grounds.auth.Role;
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
    if (!mayClaim()) {
      return false;
    }
    thing.setAttr(AttrNames.OWNER, player);
    return true;
  }

  // TBD express in policy?
  private boolean mayClaim() {
    if (player.equals(Player.GOD)) {
      return true;
    }
    Set<Role> roles = thing.getUniverse().getRoles(player);
    if (roles.contains(Role.THAUMATURGE)) {
      return true;
    }
    if (thing.getOwner().isPresent()) {
      actor.sendMessage("That is already owned by someone else");
      return false;
    }
    return true;
  }

  public static ClaimCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Optional<Thing> setThing = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
    if (!setThing.isPresent()) {
      throw new CommandFactoryException("Failed to find thing in universe");
    }
    return new ClaimCommand(actor, player, setThing.get());
  }
}
