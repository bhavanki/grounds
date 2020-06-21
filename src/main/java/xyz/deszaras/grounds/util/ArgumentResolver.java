package xyz.deszaras.grounds.util;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

/**
 * Resolves string arguments into things, in the context of another "context
 * thing" like a player.<p>
 *
 * The purpose of the resolver is to allow natural player inputs. It's not good
 * enough to force a player to refer to something, say, in the same location as
 * themselves by its ID; that something's name should be enough. The resolver
 * has the smarts to figure out what the player means.<p>
 *
 * The resolver does need to be told the type of thing that is expected. (That
 * could be {@link Thing} if the type isn't important.)<p>
 *
 * These are the rules that the resolver follows.
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
 * <li>If no nearby things match, and the thing is the GOD player or is a
 *     wizard, then the resolver falls back to looking throughout the universe.
 * <li>If two things in a set have a matching name, the resolver picks
 *     an arbitrary one.</li>
 * </ul>
 *
 * For non-contextual resolution of things by ID, use
 * {@link Universe#getThing(UUID)}.<p>
 */
public class ArgumentResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ArgumentResolver.class);

  /**
   * The single resolver instance. It is thread-safe.
   */
  public static final ArgumentResolver INSTANCE = new ArgumentResolver();

  private ArgumentResolver() {
  }

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

    // If the string is a UUID, resolve as a UUID.
    if (UUIDUtils.isUUID(nameOrId)) {
      return resolve(UUIDUtils.getUUID(nameOrId), type, context);
    }
    String name = nameOrId;

    // "me"
    // The context thing's class must be the same as or a superclass of the
    // expected type.
    if (name.equalsIgnoreCase(ME) && type.isAssignableFrom(context.getClass())) {
      LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), context.getId());
      return type.cast(context);
    }

    // "here"
    // The expected type must be Place.
    Optional<Place> location;
    try {
      location = context.getLocation();
      if (name.equalsIgnoreCase(HERE) && type.isAssignableFrom(Place.class) &&
          location.isPresent()) {
        LOG.debug("Resolved {} to {} {}", name, type.getSimpleName(), location.get().getId());
        return type.cast(location.get());
      }
    } catch (MissingThingException e) {
      LOG.debug("Context location is missing, so cannot resolve 'here'");
      location = Optional.empty();
    }

    // Try to resolve among the context thing's contents.
    Optional<T> contentThing = resolveAmong(name, null, type, context.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in context thing's contents",
                name, type.getSimpleName(), contentThing.get().getId());
      return contentThing.get();
    }

    // If the context thing is located somewhere, try to resolve among the
    // things in that location.
    if (location.isPresent()) {
      Optional<T> nearbyThing = resolveAmong(name, null, type, location.get().getContents());
      if (nearbyThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} nearby",
                  name, type.getSimpleName(), nearbyThing.get().getId());
        return nearbyThing.get();
      }
    }

    // If the context thing is a wizard (or GOD), then try to resolve among all
    // things in the universe.
    // TBD: allow for wizard things, not just players
    if (context instanceof Player && Role.isWizard((Player) context)) {
      Optional<T> finalThing = Universe.getCurrent().getThingByName(name, type);
      if (finalThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} in universe",
                  name, type.getSimpleName(), finalThing.get().getId());
        return finalThing.get();
      }
    }

    // Out of ideas!
    throw new ArgumentResolverException("I don't see anything named " + name);
  }

  /**
   * Resolves an ID into a thing.
   *
   * @param id ID for a thing
   * @param type expected type of thing to resolve
   * @param context thing providing context for resolution
   * @return resolved thing
   * @throws ArgumentResolverException if the thing cannot be resolved
   */
  public <T extends Thing> T resolve(UUID id, Class<T> type,
        Thing context) throws ArgumentResolverException {

    // the context thing's own ID (analogous to "me")
    // The context thing's class must be the same as or a superclass of the
    // expected type.
    if (id.equals(context.getId()) && type.isAssignableFrom(context.getClass())) {
      LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), context.getId());
      return type.cast(context);
    }

    // the context thing's location (analogous to "here")
    // The expected type must be Place.
    Optional<Place> location;
    try {
      location = context.getLocation();
      if (location.isPresent() && id.equals(location.get().getId()) &&
          type.equals(Place.class)) {
        LOG.debug("Resolved {} to {} {}", id, type.getSimpleName(), location.get().getId());
        return type.cast(location.get());
      }
    } catch (MissingThingException e) {
      LOG.debug("Context location is missing, so cannot resolve it");
      location = Optional.empty();
    }

    // Try to resolve among the context thing's contents.
    Optional<T> contentThing = resolveAmong(null, id, type, context.getContents());
    if (contentThing.isPresent()) {
      LOG.debug("Resolved {} to {} {} in context thing's contents",
                id, type.getSimpleName(), contentThing.get().getId());
      return contentThing.get();
    }

    // If the context thing is located somewhere, try to resolve among the
    // things in that location.
    if (location.isPresent()) {
      Optional<T> nearbyThing = resolveAmong(null, id, type, location.get().getContents());
      if (nearbyThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} nearby",
                  id, type.getSimpleName(), nearbyThing.get().getId());
        return nearbyThing.get();
      }
    }

    // If the context thing is a wizard (or GOD), then try to resolve among all
    // things in the universe.
    // TBD: allow for wizard things, not just players
    if (context instanceof Player && Role.isWizard((Player) context)) {
      Optional<T> finalThing = Universe.getCurrent().getThing(id, type);
      if (finalThing.isPresent()) {
        LOG.debug("Resolved {} to {} {} in universe",
                  id, type.getSimpleName(), finalThing.get().getId());
        return finalThing.get();
      }
    }

    // Out of ideas!
    throw new ArgumentResolverException("I don't see anything with ID " + id);
  }

  /**
   * Attempts to resolve a name or ID into a thing from multiple potential
   * matches. Either a name or ID must be provided, but not both.
   *
   * @param  name     name for a thing
   * @param  id       ID for a thing
   * @param  type     expected type of thing to resolve
   * @param  ids      IDs of candidate things
   * @return          resolved thing (empty if unresolved)
   */
  private <T extends Thing> Optional<T> resolveAmong(String name,
      UUID id, Class<T> type, Iterable<UUID> ids) {
    for (UUID iid : ids) {
      // Find the candidate thing.
      Optional<Thing> thingOpt = Universe.getCurrent().getThing(iid);
      if (thingOpt.isEmpty()) {
        continue;
      }
      Thing thing = thingOpt.get();
      // Check its type.
      if (!type.isAssignableFrom(thing.getClass())) {
        continue;
      }
      // Check if it matches the name / ID.
      if ((name != null && thing.getName().equalsIgnoreCase(name)) || // NOPMD
          thing.getId().equals(id)) {
        return Optional.of(type.cast(thing));
      }
    }
    return Optional.empty();
  }
}
