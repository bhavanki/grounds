package xyz.deszaras.grounds.command.combat;

import java.util.List;
import java.util.Objects;
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
 * Removes an NPC from a combat team in the player's current location.<p>
 *
 * Arguments: NPC name, team name
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class RemoveCombatNpcCommand extends Command<String> {

  private final String npcName;
  private final String teamName;

  public RemoveCombatNpcCommand(Actor actor, Player player, String npcName,
                                String teamName) {
    super(actor, player);
    this.npcName = Objects.requireNonNull(npcName);
    this.teamName = Objects.requireNonNull(teamName);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("remove an NPC from combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    try {
      combat.removePlayer(npcName, teamName);
      CombatCommand.messageAllCombatants(combat, npcName +
                                         " is removed from team " + teamName);
      return "Removed " + npcName + " from team " + teamName;
    } catch (IllegalStateException e) {
      throw new CommandException(e);
    }
  }

  public static RemoveCombatNpcCommand newCommand(Actor actor, Player player,
                                                  List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new RemoveCombatNpcCommand(actor, player, commandArgs.get(0),
                                      commandArgs.get(1));
  }
}
