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
 * Gets the roles for a player.<p>
 *
 * Arguments: target player
 */
@PermittedRoles(roles = { Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class GetRolesCommand extends Command<String> {

  private final Player targetPlayer;

  public GetRolesCommand(Actor actor, Player player, Player targetPlayer) {
    super(actor, player);
    this.targetPlayer = Objects.requireNonNull(targetPlayer);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Set<Role> newRoles = Universe.getCurrent().getRoles(targetPlayer);
    return RoleCommand.reportRoles(actor, targetPlayer, newRoles);
  }

  public static GetRolesCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);

    Player targetPlayer =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);

    return new GetRolesCommand(actor, player, targetPlayer);
  }
}
