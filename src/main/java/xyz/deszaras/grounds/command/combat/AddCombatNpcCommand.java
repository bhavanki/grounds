package xyz.deszaras.grounds.command.combat;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.combat.Npc;
import xyz.deszaras.grounds.combat.System;
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

  private final String npcName;
  private final List<String> npcBuildArgs;
  private final String teamName;

  public AddCombatNpcCommand(Actor actor, Player player, String npcName,
                             List<String> npcBuildArgs, String teamName) {
    super(actor, player);
    this.npcName = Objects.requireNonNull(npcName);
    this.npcBuildArgs = ImmutableList.copyOf(npcBuildArgs);
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
      Npc npc = combat.getSystem().buildNpc(npcName, npcBuildArgs);
      combat.addPlayer(npc, teamName);
      CombatCommand.messageAllCombatants(combat, npcName +
                                         " is added to team " + teamName);
      return "Added " + npcName + " to team " + teamName;
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new CommandException(e.getMessage());
    }
  }

  public static AddCombatNpcCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    String npcName = commandArgs.get(0);
    String teamName = commandArgs.get(commandArgs.size() - 1);
    List<String> npcBuildArgs = commandArgs.subList(1, commandArgs.size() - 1);
    return new AddCombatNpcCommand(actor, player, npcName, npcBuildArgs,
                                   teamName);
  }
}
