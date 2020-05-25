package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

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
}
