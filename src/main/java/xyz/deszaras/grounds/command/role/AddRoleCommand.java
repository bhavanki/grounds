package xyz.deszaras.grounds.command.role;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandArgumentResolver;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.RoleCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Adds a role to a player.<p>
 *
 * Arguments: role and target player<br>
 * Checks: player is GOD or THAUMATURGE
 */
public class AddRoleCommand extends Command<String> {

  private final Role role;
  private final Player targetPlayer;

  public AddRoleCommand(Actor actor, Player player, Role role,
                        Player targetPlayer) {
    super(actor, player);
    this.role = Objects.requireNonNull(role);
    this.targetPlayer = Objects.requireNonNull(targetPlayer);
  }

  @Override
  public String execute() throws CommandException {
    checkIfAnyRole("You are not a thaumaturge, so you may not " +
                   "add roles", Role.THAUMATURGE);

    Set<Role> newRoles = Universe.getCurrent().addRole(role, targetPlayer);
    return RoleCommand.reportRoles(actor, targetPlayer, newRoles);
  }

  public static AddRoleCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);

    Role role;
    try {
      role = Role.valueOf(commandArgs.get(0).toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Not a role: " + commandArgs.get(0));
    }
    Player targetPlayer =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(1), Player.class, player);

    return new AddRoleCommand(actor, player, role, targetPlayer);
  }
}
