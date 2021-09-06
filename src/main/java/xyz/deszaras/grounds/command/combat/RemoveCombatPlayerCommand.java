package xyz.deszaras.grounds.command.combat;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CombatCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandArgumentResolver;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Adds a player to a combat team in the player's current location.<p>
 *
 * Arguments: player name, team name
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class RemoveCombatPlayerCommand extends Command<String> {

  private final Player teamPlayer;
  private final String teamName;

  public RemoveCombatPlayerCommand(Actor actor, Player player, Player teamPlayer,
                                String teamName) {
    super(actor, player);
    this.teamPlayer = Objects.requireNonNull(teamPlayer);
    this.teamName = Objects.requireNonNull(teamName);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("remove a player from combat");
    Combat combat = CombatCommand.findCombatOrFail(location);

    try {
      combat.removePlayer(teamPlayer, teamName);
      return "Removed " + teamPlayer.getName() + " from team " + teamName;
    } catch (IllegalStateException e) {
      throw new CommandException(e);
    }
  }

  public static RemoveCombatPlayerCommand newCommand(Actor actor, Player player,
                                                  List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Player teamPlayer =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    return new RemoveCombatPlayerCommand(actor, player, teamPlayer,
                                         commandArgs.get(1));
  }
}
