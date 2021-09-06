package xyz.deszaras.grounds.command.combat;

import java.util.List;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CombatCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Ends a existing combat in the player's current location. Only the combat
 * owner or a wizard can end combat.<p>
 *
 * Arguments: none
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class EndCombatCommand extends Command<String> {

  public EndCombatCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("initialize combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      return "You lack WRITE permission to the combat here, so you may not end it";
    }

    location.take(combat);
    Universe.getCurrent().removeThing(combat);

    return "Removed combat " + combat.getName() + " at " + location.getName();
  }

  public static EndCombatCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    return new EndCombatCommand(actor, player);
  }
}
