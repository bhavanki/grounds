package xyz.deszaras.grounds.command.combat;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.combat.Npc;
import xyz.deszaras.grounds.combat.grapple.GrappleSystem;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CombatCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Adds an NPC to a combat team in the player's current location.<p>
 *
 * Arguments: NPC name, team name
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class AddCombatNpcCommand extends Command<String> {

  private final Npc npc;
  private final String teamName;

  public AddCombatNpcCommand(Actor actor, Player player, Npc npc, String teamName) {
    super(actor, player);
    this.npc = Objects.requireNonNull(npc);
    this.teamName = Objects.requireNonNull(teamName);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("add an NPC to combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      return "You lack WRITE permission to the combat here, so you may not " +
      "add an NPC to it";
    }

    try {
      combat.addPlayer(npc, teamName);
      CombatCommand.messageAllCombatants(combat, npc.getName() +
                                         " is added to team " + teamName);
      return "Added " + npc.getName() + " to team " + teamName;
    } catch (IllegalStateException e) {
      throw new CommandException(e);
    }
  }

  public static AddCombatNpcCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 3);
    // FIXME get system from combat and build NPC then
    Npc npc = new GrappleSystem().buildNpc(commandArgs.get(0),
                                           List.of(commandArgs.get(1)));
    return new AddCombatNpcCommand(actor, player, npc, commandArgs.get(2));
  }
}
