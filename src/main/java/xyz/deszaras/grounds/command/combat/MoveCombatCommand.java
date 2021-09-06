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
import xyz.deszaras.grounds.model.Universe;

/**
 * Moves within a existing combat in the player's current location.<p>
 *
 * Arguments: move command to be passed to engine
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class MoveCombatCommand extends Command<String> {

  private final List<String> moveCommand;

  public MoveCombatCommand(Actor actor, Player player, List<String> moveCommand) {
    super(actor, player);
    this.moveCommand = ImmutableList.copyOf(moveCommand);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("move in combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    String moveResult = combat.move(player, moveCommand);
    StringBuilder b = new StringBuilder();
    b.append(player.getName()).append(" moves: ").append(moveResult);
    b.append("\n\n").append(combat.status());
    CombatCommand.messageAllCombatants(combat, b.toString());

    return moveResult;
  }

  public static MoveCombatCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new MoveCombatCommand(actor, player, commandArgs);
  }
}
