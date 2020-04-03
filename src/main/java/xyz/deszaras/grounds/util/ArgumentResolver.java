package xyz.deszaras.grounds.util;

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
 * Resolves string command arguments into things, in the context of
 * another "context thing" like a player. Resolution only works within the
 * context thing's universe.<p>
 *
 * <ul>
 * <li>A string that appears to be a UUID is treated as a thing's ID;
 *     otherwise, it is treated as a thing's name, or as a special value
 *     as described next.</li>
 * <li>The string "me" resolves to the context thing (if the types are
 *     compatible).</li>
 * <li>The string "here" resolves to the context thing's location.</li>
 * <li>The resolver looks through the context things's contents for a
 *     name / ID match.</li>
 * <li>If no contents match, the resolver looks through the things that
 *     share the context things's location.</li>
 * <li>If no nearby things match, and the thing is the GOD player is a
 *     wizard, and the thing being resolved is specified by an ID, then
 *     the resolver falls back to looking throughout the context thing's
 *     universe.
 * <li>If two things in a set have a matching name, the resolver picks
 *     an arbitrary one.</li>
 * </ul>
 *
 * For non-contextual resolution of things by ID, use
 * {@link Universe#getThing(UUID)} and/or {@link Multiverse#findThing(UUID)}.<p>
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
   * @param context thing providing context for resolution
   * @return resolved thing
   * @throws ArgumentResolverException if the thing cannot be resolved
   */
  public <T extends Thing> T resolve(String nameOrId,
      Class<T> type, Thing context) throws ArgumentResolverException {

    if (UUID_PATTERN.matcher(nameOrId).matches()) {
      return resolve(UUID.fromString(nameOrId), type, context);
    }
    String name = nameOrId;

    if (name.equalsIgnoreCase(ME) && type.isAssignableFrom(context.getClass())) {
      LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), context.getId());
      return type.cast(context);
    }

    Optional<Place> location = context.getLocation();
    if (name.equalsIgnoreCase(HERE) && type.isAssignableFrom(Place.class) &&
        location.isPresent()) {
      LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), location.get().getId());
      return type.cast(location.get());
    }

    Universe universe = context.getUniverse();

    Optional<T> contentThing = resolveAmong(name, null, type, universe,
                                            context.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in context thing's contents",
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

    throw new ArgumentResolverException("I don't see anything named " + name);
  }

  /**
   * Resolves an ID into a thing.
   *
   * @param id id for a thing
   * @param type expected type of thing to resolve
   * @param context thing providing context for resolution
   * @return resolved thing
   * @throws ArgumentResolverException if the thing cannot be resolved
   */
  public <T extends Thing> T resolve(UUID id, Class<T> type,
        Thing context) throws ArgumentResolverException {
    if (id.equals(context.getId()) && type.isAssignableFrom(context.getClass())) {
      LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), context.getId());
      return type.cast(context);
    }

    Optional<Place> location = context.getLocation();
    if (location.isPresent() && id.equals(location.get().getId()) &&
        type.equals(Place.class)) {
      LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), location.get().getId());
      return type.cast(location.get());
    }

    Universe universe = context.getUniverse();

    Optional<T> contentThing = resolveAmong(null, id, type, universe,
                                            context.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in context thing's contents",
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

    // TBD: allow for wizard things, not just players
    if (context instanceof Player && Role.isWizard((Player) context)) {
      Optional<T> finalThing = universe.getThing(id, type);
      if (finalThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} in universe",
                  id, type.getSimpleName(), finalThing.get().getId());
        return finalThing.get();
      }
    }

    throw new ArgumentResolverException("I don't see anything with ID " + id);
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
      if ((name != null && thing.getName().equalsIgnoreCase(name)) || // NOPMD
          thing.getId().equals(id)) {
        return Optional.of(type.cast(thing));
      }
    }
    return Optional.empty();
  }
}