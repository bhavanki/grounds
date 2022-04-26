package xyz.deszaras.grounds.command.combat;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

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

/**
 * Restores the state of combat.
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class RestoreCombatCommand extends Command<String> {

  public RestoreCombatCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("restore combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      throw new CommandException("You lack WRITE permission to the combat here, so you may not restore it");
    }

    Optional<String> stateString = combat.getState();
    if (stateString.isEmpty()) {
      throw new CommandException("Combat state not found");
    }

    try {
      byte[] state = Base64.getDecoder().decode(stateString.get());
      combat.setEngine(combat.getSystem().restore(state));
      CombatCommand.messageAllCombatants(combat,
                                         "Combat has been restored to a prior state");
      return "Combat restored";
    } catch (IllegalArgumentException | UnsupportedOperationException e) {
      throw new CommandException("Failed to restore combat state", e);
    }
  }

  public static RestoreCombatCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    return new RestoreCombatCommand(actor, player);
  }
}
