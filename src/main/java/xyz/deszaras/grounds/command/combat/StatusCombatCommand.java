package xyz.deszaras.grounds.command.combat;

import java.util.List;
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
 * Reports the status of an existing combat in the player's current location.<p>
 *
 * Arguments: none
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class StatusCombatCommand extends Command<String> {

  public StatusCombatCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("get combat status");
    Combat combat = CombatCommand.findCombatOrFail(location);

    return combat.status();
  }

  public static StatusCombatCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    return new StatusCombatCommand(actor, player);
  }
}
