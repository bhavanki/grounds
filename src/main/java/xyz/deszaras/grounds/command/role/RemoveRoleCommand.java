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
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.command.RoleCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Removes a role from a player.<p>
 *
 * Arguments: role and target player
 */
@PermittedRoles(roles = { Role.THAUMATURGE })
public class RemoveRoleCommand extends Command<String> {

  private final Role role;
  private final Player targetPlayer;

  public RemoveRoleCommand(Actor actor, Player player, Role role,
                           Player targetPlayer) {
    super(actor, player);
    this.role = Objects.requireNonNull(role);
    this.targetPlayer = Objects.requireNonNull(targetPlayer);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Set<Role> newRoles = Universe.getCurrent().removeRole(role, targetPlayer);
    return RoleCommand.reportRoles(actor, targetPlayer, newRoles);
  }

  public static RemoveRoleCommand newCommand(Actor actor, Player player,
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

    return new RemoveRoleCommand(actor, player, role, targetPlayer);
  }
}
