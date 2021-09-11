package xyz.deszaras.grounds.command.combat;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CombatCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Moves an NPC within a existing combat in the player's current location.<p>
 *
 * Arguments: NPC name, move command to be passed to engine
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class MoveNpcCombatCommand extends Command<Boolean> {

  private final String npcName;
  private final List<String> moveCommand;

  public MoveNpcCombatCommand(Actor actor, Player player, String npcName,
                           List<String> moveCommand) {
    super(actor, player);
    this.npcName = Objects.requireNonNull(npcName);
    this.moveCommand = ImmutableList.copyOf(moveCommand);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    Place location = getPlayerLocation("move in combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    if (!combat.passes(Category.WRITE, player)) {
      Message errorMessage = newMessage(Message.Style.COMMAND_EXCEPTION,
          "You lack WRITE permission to the combat here, so you may not " +
          "move an NPC in it");
      player.sendMessage(errorMessage);
      return false;
    }

    String moveResult = combat.move(npcName, moveCommand);
    StringBuilder b = new StringBuilder();
    b.append(npcName).append(" moves: ").append(moveResult);
    b.append("\n\n").append(combat.status());
    CombatCommand.messageAllCombatants(combat, b.toString());

    return true;
  }

  public static MoveNpcCombatCommand newCommand(Actor actor, Player player,
                                                List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new MoveNpcCombatCommand(actor, player, commandArgs.get(0),
                                    commandArgs.subList(1, commandArgs.size()));
  }
}
