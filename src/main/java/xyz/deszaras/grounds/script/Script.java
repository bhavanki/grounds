package xyz.deszaras.grounds.script;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.util.ArgumentResolver;
import xyz.deszaras.grounds.util.ArgumentResolverException;

/**
 * A script to be run by the application. It could be the implementation
 * of a command, or the content of a listener attribute on a thing.
 */
public class Script {

  /**
   * Script argument types.
   */
  public enum ArgType {
    STRING,
    INTEGER,
    BOOLEAN,
    THING,
    PLAYER,
    PLACE;
  }

  private final String content;
  private final List<ArgType> scriptArgumentTypes;

  /**
   * Creates a new script.
   *
   * @param content script content
   * @param scriptArgumentTypes types for each script argument
   * @throws NullPointerException if any argument is null
   */
  public Script(String content, List<ArgType> scriptArgumentTypes) {
    this.content = Objects.requireNonNull(content);
    this.scriptArgumentTypes = ImmutableList.copyOf(scriptArgumentTypes);
  }

  /**
   * Gets the script content.
   *
   * @return content
   */
  public String getContent() {
    return content;
  }

  /**
   * Gets the list of script argument types.
   *
   * @return script argument types
   */
  public List<ArgType> getScriptArgumentTypes() {
    return scriptArgumentTypes;
  }

  /**
   * Resolves string arguments to this script, using the
   * {@link ArgumentResolver} for things in the universe, or else
   * performing necessary type conversions. The "context thing" for argument
   * resolution is the caller.
   *
   * @param stringArguments string arguments to the script
   * @param caller script caller, used for context in resolution
   * @return resolved arguments
   * @throws IllegalArgumentException if the wrong number of arguments is passed
   *     to this method
   * @throws ArgumentResolverException if argument resolution fails
   */
  public List<Object> resolveScriptArguments(List<String> stringArguments,
                                             Thing caller)
      throws ArgumentResolverException {
    if (stringArguments.size() != scriptArgumentTypes.size()) {
      throw new IllegalArgumentException("The script expects " +
          scriptArgumentTypes.size() + " arguments, but was given " +
          stringArguments.size());
    }

    List<Object> resolvedArguments = new ArrayList<>();
    int idx = 0;
    for (ArgType type : scriptArgumentTypes) {

      // Convert the passed command argument string into the expected
      // object type, and add it to the list of arguments to pass to
      // the scripted command.
      String arg = stringArguments.get(idx++);
      switch (type) {
        case STRING:
          resolvedArguments.add(arg);
          break;
        case INTEGER:
          resolvedArguments.add(Integer.valueOf(arg));
          break;
        case BOOLEAN:
          resolvedArguments.add(Boolean.valueOf(arg));
          break;
        case THING:
          resolvedArguments.add(
              ArgumentResolver.INSTANCE.resolve(arg, Thing.class, caller));
          break;
        case PLAYER:
          resolvedArguments.add(
              ArgumentResolver.INSTANCE.resolve(arg, Player.class, caller));
          break;
        case PLACE:
          resolvedArguments.add(
              ArgumentResolver.INSTANCE.resolve(arg, Place.class, caller));
          break;
        default:
          throw new ArgumentResolverException("Unsupported argument type " + type);
      }
    }

    return ImmutableList.copyOf(resolvedArguments);
  }
}
