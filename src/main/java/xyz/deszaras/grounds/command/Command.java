package xyz.deszaras.grounds.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(Command.class);

  protected final Actor actor;
  protected final Player player;
  private final Set<Event> events;

  /**
   * Creates a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   */
  protected Command(Actor actor, Player player) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
    events = new HashSet<>();
  }

  /**
   * Executes the command. This does work around the call to
   * {@link #execute()}, so outside code must call this method.
   *
   * @return result of command
   * @throws CommandException if the command fails
   */
  public R execute() throws CommandException {
    checkPermittedRoles();
    try {
      return executeImpl();
    } catch (RuntimeException e) {
      LOG.info("Unexpected command exception", e);
      throw new CommandException(e);
    }
  }

  /**
   * Executes the implementation of this command.
   *
   * @return result of command
   * @throws CommandException if the command fails
   */
  protected abstract R executeImpl() throws CommandException;

  /**
   * Remembers the given event. Use this during command execute to record an
   * event to be published for listeners.
   *
   * @param es events to remember
   */
  public void postEvent(Event e) {
    postEvents(Set.of(e));
  }

  /**
   * Remembers the given events. Use this during command execute to record
   * events to be published for listeners.
   *
   * @param es events to remember
   */
  public void postEvents(Collection<Event> es) {
    events.addAll(es);
  }

  /**
   * Gets the events remembered during command execution.
   *
   * @return remembered events
   */
  public Set<Event> getEvents() {
    return events;
  }

  /**
   * Checks if the player running the command has any of the given roles.
   *
   * @param roles roles to check for
   * @return true if player has at least one of the given roles
   */
  protected boolean checkIfAnyRole(Role... roles) {
    if (player.equals(Player.GOD)) {
      return true;
    }
    Set<Role> playerRoles = Universe.getCurrent().getRoles(player);
    return Arrays.stream(roles).anyMatch(r -> playerRoles.contains(r));
  }

  /**
   * Checks if the player running the command has any of the given roles.
   *
   * @param roles roles to check for
   * @return true if player has at least one of the given roles
   */
  protected boolean checkIfAnyRole(Collection<Role> roles) {
    if (player.equals(Player.GOD)) {
      return true;
    }
    Set<Role> playerRoles = Universe.getCurrent().getRoles(player);
    return roles.stream().anyMatch(r -> playerRoles.contains(r));
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
    if (!checkIfAnyRole(roles)) {
      throw new PermissionException(message);
    }
  }

  /**
   * Checks if the player running the command has any of the roles in the
   * command's {@link PermittedRoles} annotation, and if not, throws a
   * {@link PermissionException} with the annotation's failure message.
   *
   * @throws PermissionException if the permission check fails
   */
  protected void checkPermittedRoles() throws PermissionException {
    PermittedRoles annotation = this.getClass().getAnnotation(PermittedRoles.class);
    if (annotation == null) {
      throw new PermissionException("@PermittedRoles annotation missing on command " +
                                    "class " + this.getClass().getName());
    }

    checkIfAnyRole(annotation.failureMessage(), annotation.roles());
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
      Optional<Place> locationOpt = player.getLocationAsPlace();
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
