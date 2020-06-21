package xyz.deszaras.grounds.command;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * A command to observe or make a change to a universe.
 *
 * @param R type of command return value
 */
public abstract class Command<R> {

  protected final Actor actor;
  protected final Player player;

  /**
   * Creates a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   */
  protected Command(Actor actor, Player player) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
  }

  /**
   * Executes the command.
   *
   * @return result of command
   * @throws CommandException if the command fails
   */
  public abstract R execute() throws CommandException;

  /**
   * Checks if the player running the command is a wizard, and if not, throws a
   * {@link PermissionException}.
   *
   * @param message message to send to actor if permission check fails
   * @throws PermissionException if the permission check fails
   */
  protected void checkIfWizard(String message) throws PermissionException {
    if (!Role.isWizard(player)) {
      throw new PermissionException(message);
    }
  }

  private static final Role[] NON_GUEST_ROLES_ARRAY =
      Role.NON_GUEST_ROLES.toArray(new Role[Role.NON_GUEST_ROLES.size()]);

  /**
   * Checks if the player running the command is a non-guest, and if not, throws
   * a {@link PermissionException}.
   *
   * @param message message to send to actor if permission check fails
   * @throws PermissionException if the permission check fails
   */
  protected void checkIfNonGuest(String message) throws PermissionException {
    checkIfAnyRole(message, NON_GUEST_ROLES_ARRAY);
  }

  /**
   * Checks if the player running the command has any of the given roles, and if
   * not, throws a {@link PermissionException}.
   *
   * @param message message to send to actor if permission check fails
   * @param roles roles to check for
   * @throws PermissionException if the permission check fails
   */
  protected void checkIfAnyRole(String message, Role... roles)
      throws PermissionException {
    if (player.equals(Player.GOD)) {
      return;
    }
    Set<Role> playerRoles = Universe.getCurrent().getRoles(player);
    if (Arrays.stream(roles).noneMatch(r -> playerRoles.contains(r))) {
      throw new PermissionException(message);
    }
  }

  /**
   * Checks if the player running the command passes a permission on a thing,
   * and if not, throws a {@link PermissionException}.
   *
   * @param category category to check permission for
   * @param thing thing to check permission for
   * @param message message to send to actor if permission check fails
   * @throws PermissionException if the permission check fails
   */
  protected void checkPermission(Category category, Thing thing, String message)
      throws PermissionException {
    if (!thing.passes(category, player)) {
      throw new PermissionException(message);
    }
  }

  /**
   * Ensures that a list has a minimum length.
   *
   * @param l list
   * @param n minimum length
   * @throws CommandFactoryException if the list does not have enough elements
   */
  protected static void ensureMinArgs(List<String> l, int n) throws CommandFactoryException {
    if (l.size() < n) {
      throw new CommandFactoryException("Need at least " + n + " arguments, got " + l.size());
    }
  }

  /**
   * Gets the player's location, failing gracefully if either the player
   * has no location or that location cannot be found.
   *
   * @param  action           verb phrase used for exception messages
   * @return                  player location
   * @throws CommandException if the player has no location or it cannot be found
   */
  protected Place getPlayerLocation(String action) throws CommandException {
    try {
      Optional<Place> locationOpt = player.getLocation();
      if (locationOpt.isEmpty()) {
        throw new CommandException("You have no location, so you may not " + action);
      }
      return locationOpt.get();
    } catch (MissingThingException e) {
      throw new CommandException("I cannot determine your location!");
    }
  }

  protected Message newMessage(Message.Style style, String message) {
    return new Message(player, style, message);
  }

  protected Message newInfoMessage(String message) {
    return new Message(player, Message.Style.INFO, message);
  }
}
