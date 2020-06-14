package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xyz.deszaras.grounds.command.role.AddRoleCommand;
import xyz.deszaras.grounds.command.role.GetRolesCommand;
import xyz.deszaras.grounds.command.role.RemoveRoleCommand;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class RoleCommand extends Command<Boolean> {

  public RoleCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public Boolean execute() {
    throw new UnsupportedOperationException("This is a composite command");
  }

  public static void checkIfThaumaturge(Actor actor, Player player,
                                        Player targetPlayer)
      throws PermissionException{
    if (!player.equals(Player.GOD)) {
      Set<Role> roles = Universe.getCurrent().getRoles(player);
      if (!roles.contains(Role.THAUMATURGE)) {
        throw new PermissionException("You are not a thaumaturge," +
                                      " so you may not work with roles there");
      }
    }
  }

  public static String reportRoles(Actor actor, Player targetPlayer,
                                 Set<Role> newRoles) {
    return String.format("Roles for %s: %s", targetPlayer.getName(),
                         newRoles.toString());
  }

  private static final Map<String, Class<? extends Command>> ROLE_COMMANDS;

  static {
    ROLE_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("ADD", AddRoleCommand.class)
        .put("GRANT", AddRoleCommand.class)
        .put("GET", GetRolesCommand.class)
        .put("SHOW", GetRolesCommand.class)
        .put("REMOVE", RemoveRoleCommand.class)
        .put("REVOKE", RemoveRoleCommand.class)
        .build();
  }

  private static final CommandFactory ROLE_COMMAND_FACTORY =
      new CommandFactory(null, ROLE_COMMANDS, null);

  public static Command newCommand(Actor actor, Player player,
                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return ROLE_COMMAND_FACTORY.getCommand(actor, player, commandArgs);
  }
}
