package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Grants ownership of a thing to a player.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player is GOD or THAUMATURGE; thing is not already owned
 */
public class ClaimCommand extends Command<Boolean> {

  private final Thing thing;

  public ClaimCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  public Boolean execute() throws CommandException {
    checkClaim();
    thing.setOwner(player);
    return true;
  }

  // TBD express in policy?
  private void checkClaim() throws PermissionException {
    Optional<Thing> owner;
    try {
      owner = thing.getOwner();
    } catch (MissingThingException e) {
      return; // missing owner => unowned
    }
    if (owner.isPresent()) {
      if (owner.get().equals(player)) {
        player.sendMessage(newInfoMessage("You already own that"));
        return;
      }
      checkIfAnyRole("That is already owned by someone else", Role.THAUMATURGE);
    }
  }

  public static ClaimCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing claimedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new ClaimCommand(actor, player, claimedThing);
  }
}
