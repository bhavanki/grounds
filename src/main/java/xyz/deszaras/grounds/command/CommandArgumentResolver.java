package xyz.deszaras.grounds.command;

import java.util.UUID;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.util.ArgumentResolver;
import xyz.deszaras.grounds.util.ArgumentResolverException;

/**
 * A wrapper around {@link ArgumentResolver} that just wraps
 * exceptions.
 */
public class CommandArgumentResolver {

  /**
   * The single resolver instance. It is thread-safe.
   */
  public static final CommandArgumentResolver INSTANCE = new CommandArgumentResolver();

  private CommandArgumentResolver() {
  }

  public <T extends Thing> T resolve(String nameOrId,
      Class<T> type, Thing context) throws CommandFactoryException {
    try {
      return ArgumentResolver.INSTANCE.resolve(nameOrId, type, context);
    } catch (ArgumentResolverException e) {
      throw new CommandFactoryException("Failed to resolve command arguments", e);
    }
  }

  public <T extends Thing> T resolve(UUID id, Class<T> type,
        Thing context) throws CommandFactoryException {
    try {
      return ArgumentResolver.INSTANCE.resolve(id, type, context);
    } catch (ArgumentResolverException e) {
      throw new CommandFactoryException("Failed to resolve command arguments", e);
    }
  }

}
