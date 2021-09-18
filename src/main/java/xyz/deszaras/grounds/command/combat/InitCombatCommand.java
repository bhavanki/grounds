package xyz.deszaras.grounds.command.combat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
 * Initializes a new combat in the player's current location. The player becomes
 * the referee (owner) of the combat.<p>
 *
 * Arguments: combat name
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class InitCombatCommand extends Command<String> {

  private final String name;

  public InitCombatCommand(Actor actor, Player player, String name) {
    super(actor, player);
    this.name = Objects.requireNonNull(name);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Place location = getPlayerLocation("initialize combat");
    Optional<Combat> existingCombat = CombatCommand.findCombat(location);
    if (existingCombat.isPresent()) {
      throw new CommandException("Combat " + existingCombat.get().getName() +
                                 " is already present here");
    }

    Combat newCombat = new Combat(name);
    newCombat.setOwner(player);
    Universe.getCurrent().addThing(newCombat);
    newCombat.setLocation(location);
    location.give(newCombat);

    return "Created combat " + name + " at " + location.getName() +
        ". Add players to teams, and then start combat.";
  }

  public static InitCombatCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new InitCombatCommand(actor, player, commandArgs.get(0));
  }
}
