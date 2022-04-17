package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Revokes ownership of a thing from a player.<p>
 *
 * Arguments: name or ID of thing<br>
 * Checks: player owns thing
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class AbandonCommand extends Command<Boolean> {

  private final Thing thing;

  public AbandonCommand(Actor actor, Player player, Thing thing) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    Thing owner;
    try {
      owner = thing.getOwner().orElse(null);
    } catch (MissingThingException e) {
      // Whatever the owner is, it isn't the player
      // throw new CommandException("You do not own that");
      throw new PermissionException("Cannot find the current owner!");
    }

    if (owner == null) {
      player.sendMessage(newInfoMessage("No one owns that"));
      return true;
    }
    if (!player.equals(owner)) {
      checkIfAnyRole("You do not own that", Role.ADEPT, Role.THAUMATURGE);
    }
    thing.setOwner(null);
    return true;
  }

  public static AbandonCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Thing abandonedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    return new AbandonCommand(actor, player, abandonedThing);
  }
}
