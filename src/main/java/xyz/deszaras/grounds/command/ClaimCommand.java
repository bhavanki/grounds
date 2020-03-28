package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.AttrNames;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Grants ownership of a thing to a player.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player is GOD or THAUMATURGE in the thing's universe;
 * thing is not already owned
 */
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
    thing.setOwner(player);
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
      if (thing.getOwner().get().equals(player)) {
        actor.sendMessage("You already own that");
        return true;
      }
      actor.sendMessage("That is already owned by someone else");
      return false;
    }
    return true;
  }

  public static ClaimCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing claimedThing =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new ClaimCommand(actor, player, claimedThing);
  }
}
