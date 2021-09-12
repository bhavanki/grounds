package xyz.deszaras.grounds.command.combat;

import com.google.common.collect.ImmutableList;

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
 * Starts an existing combat in the player's current location. Only the combat
 * owner or a wizard can start combat.<p>
 *
 * Arguments: optional team names in moving order
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class StartCombatCommand extends Command<String> {

  private final List<String> teamNames;

  public StartCombatCommand(Actor actor, Player player, List<String> teamNames) {
    super(actor, player);
    this.teamNames = ImmutableList.copyOf(teamNames);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("start");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      return "You lack WRITE permission to the combat here, so you may not start it";
    }

    combat.start(teamNames);

    CombatCommand.messageAllCombatants(combat, "Combat has started!");
    return combat.status();
  }

  public static StartCombatCommand newCommand(Actor actor, Player player,
                                              List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 0);
    return new StartCombatCommand(actor, player, commandArgs);
  }
}
