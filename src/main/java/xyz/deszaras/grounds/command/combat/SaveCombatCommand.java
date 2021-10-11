package xyz.deszaras.grounds.command.combat;

import java.util.Base64;
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

/**
 * Saves the state of combat.
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class SaveCombatCommand extends Command<String> {

  public SaveCombatCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("save combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      return "You lack WRITE permission to the combat here, so you may not save it";
    }

    if (combat.getEngine() == null) {
      throw new CommandException("Combat has not yet started");
    }
    byte[] state = combat.getEngine().getState();
    if (state == null) {
      throw new CommandException("Saving combat is not available");
    }

    combat.setState(Base64.getEncoder().encodeToString(state));
    return "Combat saved";
  }

  public static SaveCombatCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    return new SaveCombatCommand(actor, player);
  }
}
