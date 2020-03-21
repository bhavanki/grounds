package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class RoleCommand extends Command {

  private final String actionString;
  private final List<String> roleArgs;

  public RoleCommand(Actor actor, Player player, String actionString,
                     List<String> roleArgs) {
    super(actor, player);
    this.actionString = Objects.requireNonNull(actionString);
    this.roleArgs = ImmutableList.copyOf(Objects.requireNonNull(roleArgs));
  }

  @Override
  public boolean execute() {
    Universe universe = player.getUniverse();

    Role role;
    Player targetPlayer;
    Set<Role> newRoles;
    switch (actionString.toUpperCase()) {
      case "GRANT":
      case "ADD":
        ensureExactRoleArgs(roleArgs, 2, "granting");
        role = Role.valueOf(roleArgs.get(0).toUpperCase());
        targetPlayer = findPlayer(roleArgs.get(1), universe);
        if (targetPlayer == null) {
          return false;
        }
        newRoles = universe.addRole(role, targetPlayer);
        break;
      case "REVOKE":
      case "REMOVE":
        ensureExactRoleArgs(roleArgs, 2, "revoking");
        role = Role.valueOf(roleArgs.get(0).toUpperCase());
        targetPlayer = findPlayer(roleArgs.get(1), universe);
        if (targetPlayer == null) {
          return false;
        }
        newRoles = universe.removeRole(role, targetPlayer);
        break;
      case "SHOW":
      case "GET":
        ensureExactRoleArgs(roleArgs, 1, "showing");
        targetPlayer = findPlayer(roleArgs.get(0), universe);
        if (targetPlayer == null) {
          return false;
        }
        newRoles = universe.getRoles(targetPlayer);
        break;
      default:
        throw new IllegalArgumentException("Unsupported role command " + actionString);
    }

    actor.sendMessage(String.format("Roles for %s: %s",
                                    targetPlayer.getName(),
                                    newRoles.toString()));
    return true;
  }

  private static void ensureExactRoleArgs(List<String> l, int n, String action) {
    if (l.size() != n) {
      throw new IllegalArgumentException("For " + action + " roles, expected " + n +
                                         " arguments, got " + l.size());
    }
  }

  private Player findPlayer(String targetPlayerId, Universe universe) {
    Optional<Player> targetPlayer =
        universe.getThing(UUID.fromString(targetPlayerId), Player.class);
    if (!targetPlayer.isPresent()) {
      actor.sendMessage("I can't find a player with ID " + targetPlayerId);
      return null;
    }
    return targetPlayer.get();
  }

  public static RoleCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 2);
    String actionString = commandArgs.get(0);
    List<String> roleArgs = commandArgs.subList(1, commandArgs.size());
    return new RoleCommand(actor, player, actionString, roleArgs);
  }
}
