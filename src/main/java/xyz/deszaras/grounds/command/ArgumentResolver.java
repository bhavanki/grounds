package xyz.deszaras.grounds.command;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Resolves string command arguments into things. Resolution only works
 * within a player's universe.<p>
 *
 * <ul>
 * <li>A string that appears to be a UUID is treated as a thing's ID;
 *     otherwise, it is treated as a thing's name, or as a special value
 *     as described next.</li>
 * <li>The string "me" resolves to the player.</li>
 * <li>The string "here" resolves to the player's location.</li>
 * <li>The resolver looks through the player's contents for a name / ID
 *     match.</li>
 * <li>If no player contents match, the resolver looks through the things
 *     that share the player's location.</li>
 * <li>If no nearby things match, and the player is GOD or a wizard, and
 *     tthe thing is specified by an ID, then the resolver falls back to
 *     looking throughout the player's current universe.
 * <li>If two things in a set have a matching name, the resolver picks
 *     an arbitrary one.</li>
 * </ul>
 *
 * For non-contextual resolution of things by ID, use
 * {@link Universe#getThing(UUID)} and/or {@link Multiverse#findThing(UUID)}.<p>
 *
 * Resolution failure results in throwing {@link CommandFactoryException},
 * since resolution is used while building commands.
 */
public class ArgumentResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ArgumentResolver.class);

  /**
   * The single resolver instance. It is thread-safe.
   */
  public static final ArgumentResolver INSTANCE = new ArgumentResolver();

  private ArgumentResolver() {
  }

  private static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  private static final String HERE = "here";
  private static final String ME = "me";

  /**
   * Resolves a name or ID into a thing.
   *
   * @param nameOrId name or UUID string for a thing
   * @param type expected type of thing to resolve
   * @param player player providing context for resolution
   * @return resolved thing
   * @throws CommandFactoryException if the thing cannot be resolved
   */
  public <T extends Thing> T resolve(String nameOrId,
      Class<T> type, Player player) throws CommandFactoryException {

    if (UUID_PATTERN.matcher(nameOrId).matches()) {
      return resolve(UUID.fromString(nameOrId), type, player);
    }
    String name = nameOrId;

    if (name.equalsIgnoreCase(ME) && type.isAssignableFrom(Player.class)) {
      LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), player.getId());
      return type.cast(player);
    }

    Optional<Place> location = player.getLocation();
    if (name.equalsIgnoreCase(HERE) && type.isAssignableFrom(Place.class) &&
        location.isPresent()) {
      LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), location.get().getId());
      return type.cast(location.get());
    }

    Universe universe = player.getUniverse();

    Optional<T> contentThing = resolveAmong(name, null, type, universe,
                                            player.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in player contents",
                name, type.getSimpleName(), contentThing.get().getId());
      return contentThing.get();
    }

    if (location.isPresent()) {
      Optional<T> nearbyThing = resolveAmong(name, null, type, universe,
                                             location.get().getContents());
      if (nearbyThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} nearby",
                  name, type.getSimpleName(), nearbyThing.get().getId());
        return nearbyThing.get();
      }
    }

    throw new CommandFactoryException("I don't see anything named " + name);
  }

  /**
   * Resolves an ID into a thing.
   *
   * @param id id for a thing
   * @param type expected type of thing to resolve
   * @param player player providing context for resolution
   * @return resolved thing
   * @throws CommandFactoryException if the thing cannot be resolved
   */
  public <T extends Thing> T resolve(UUID id,
      Class<T> type, Player player) throws CommandFactoryException {
    if (id.equals(player.getId()) && type.equals(Player.class)) {
      LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), player.getId());
      return type.cast(player);
    }

    Optional<Place> location = player.getLocation();
    if (location.isPresent() && id.equals(location.get().getId()) &&
        type.equals(Place.class)) {
      LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), location.get().getId());
      return type.cast(location.get());
    }

    Universe universe = player.getUniverse();

    Optional<T> contentThing = resolveAmong(null, id, type, universe,
                                            player.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in player contents",
                id, type.getSimpleName(), contentThing.get().getId());
      return contentThing.get();
    }

    if (location.isPresent()) {
      Optional<T> nearbyThing = resolveAmong(null, id, type, universe,
                                             location.get().getContents());
      if (nearbyThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} nearby",
                  id, type.getSimpleName(), nearbyThing.get().getId());
        return nearbyThing.get();
      }
    }

    if (Role.isWizard(player)) {
      Optional<T> finalThing = universe.getThing(id, type);
      if (finalThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} in universe",
                  id, type.getSimpleName(), finalThing.get().getId());
        return finalThing.get();
      }
    }

    throw new CommandFactoryException("I don't see anything with ID " + id);
  }

  private <T extends Thing> Optional<T> resolveAmong(String name,
      UUID id, Class<T> type, Universe universe, Iterable<UUID> ids) {
    for (UUID iid : ids) {
      Optional<Thing> thingOpt = universe.getThing(iid);
      if (thingOpt.isEmpty()) {
        continue;
      }
      Thing thing = thingOpt.get();
      if (!type.isAssignableFrom(thing.getClass())) {
        continue;
      }
      if ((name != null && thing.getName().equalsIgnoreCase(name)) ||
          thing.getId().equals(id)) {
        return Optional.of(type.cast(thing));
      }
    }
    return Optional.empty();
  }
}
