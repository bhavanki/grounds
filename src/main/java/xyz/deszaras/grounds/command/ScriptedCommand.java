package xyz.deszaras.grounds.command;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Thing;

/**
 * Runs a scripted command.<p>
 *
 * A scripted command is defined in an attribute of an extension
 * in the universe of the player running it. The name of the
 * attribute matches the name of the command and must start with a
 * '$' character. The attribute value must be a list of
 * attributes:<p>
 *
 * <ul>
 * <li>script (string): the script to run</li>
 * <li>argString (string): a comma-separated list of the types of
 * each argument to the script (optional, in case there are no
 * arguments)</li>
 * </ul>
 *
 * Supported argument types are enumerated by
 * {@link ScriptedCommand.ArgType}. Arguments for things in the
 * universe are resolved by {@link ArgumentResolver} as usual.<p>
 *
 * The script is expected to be written in Groovy. After doing its
 * work, it should return a true/false value for success/failure;
 * alternatively, it can return null for success. Arguments to the
 * script are supplied as objects bound as arg0, arg1, and so on.
 * The actor and player objects are bound as well.
 */
public class ScriptedCommand extends Command {

  private final String script;
  private final List<Object> commandArguments;

  /**
   * Creates a new scripted command.
   *
   * @param actor actor executing the command
   * @param player player executing the command
   * @param script script to run
   * @param commandArgument arguments to pass to the script
   */
  public ScriptedCommand(Actor actor, Player player, String script,
                         List<Object> commandArguments) {
    super(actor, player);
    this.script = script;
    this.commandArguments = ImmutableList.copyOf(commandArguments);
  }

  @Override
  public boolean execute() {

    // Create a binding for the script, including actor, player,
    // and each argument.
    Binding binding = new Binding();
    binding.setProperty("actor", actor);
    binding.setProperty("player", player);
    for (int i = 0; i < commandArguments.size(); i++) {
      binding.setProperty("arg" + i, commandArguments.get(i));
    }

    // Evaluate the script in a Groovy shell.
    GroovyShell commandShell = new GroovyShell(binding);
    Object result = commandShell.evaluate(script);

    // Derive a return value.
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      return result == null;
    }
  }

  private static final String ARG_STRING = "argString";
  private static final Splitter ARG_STRING_SPLITTER = Splitter.on(",");
  private static final String SCRIPT = "script";

  /**
   * Command argument types.
   */
  public enum ArgType {
    STRING,
    INTEGER,
    BOOLEAN,
    THING,
    PLAYER,
    PLACE;
  }

  public static ScriptedCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String commandName = commandArgs.get(0);

    // Find the attribute defining the command. It must be attached
    // to an extension in the player's universe and have an attribute
    // list as a value.
    Optional<Attr> commandAttr = Optional.empty();
    // this is inefficient :(
    for (Extension extension : player.getUniverse().getThings(Extension.class)) {
      commandAttr = extension.getAttr(commandName, Attr.Type.ATTRLIST);
      if (commandAttr.isPresent()) {
        break;
      }
    }
    if (commandAttr.isEmpty()) {
      throw new CommandFactoryException("Failed to locate command");
    }

    List<Attr> commandAttrs = commandAttr.get().getAttrListValue();

    // Get the script from the attribute list.
    Optional<Attr> scriptAttr = commandAttrs.stream()
        .filter(a -> a.getName().equals(SCRIPT) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    if (scriptAttr.isEmpty()) {
      throw new CommandFactoryException("Command is missing a script");
    }

    // Get the argString from the attribute list, if present.
    Optional<Attr> argStringAttr = commandAttrs.stream()
        .filter(a -> a.getName().equals(ARG_STRING) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();

    List<Object> commandArguments = new ArrayList<>();
    int idx = 1;
    if (argStringAttr.isPresent()) {
      // Iterate over each argument type.
      for (String argType : ARG_STRING_SPLITTER.split(argStringAttr.get().getValue())) {
        if (idx >= commandArgs.size()) {
          throw new CommandFactoryException("Not enough arguments");
        }

        // Convert the passed command argument string into the expected
        // object type, and add it to the list of arguments to pass to
        // the scripted command.
        String currentCommandArg = commandArgs.get(idx++);
        try {
          switch (ArgType.valueOf(argType.toUpperCase())) {
            case STRING:
              commandArguments.add(currentCommandArg);
              break;
            case INTEGER:
              commandArguments.add(Integer.valueOf(currentCommandArg));
              break;
            case BOOLEAN:
              commandArguments.add(Boolean.valueOf(currentCommandArg));
              break;
            case THING:
              commandArguments.add(
                  CommandArgumentResolver.INSTANCE.resolve(currentCommandArg, Thing.class, player));
              break;
            case PLAYER:
              commandArguments.add(
                  CommandArgumentResolver.INSTANCE.resolve(currentCommandArg, Player.class, player));
              break;
            case PLACE:
              commandArguments.add(
                  CommandArgumentResolver.INSTANCE.resolve(currentCommandArg, Place.class, player));
              break;
            default:
              throw new IllegalStateException("Unsupported argument type " + argType);
          }
        } catch (IllegalArgumentException e) {
          throw new CommandFactoryException("Failed to resolve command argument", e);
        }
      }
    }

    // TBD: policy ???

    return new ScriptedCommand(actor, player, scriptAttr.get().getValue(),
                               commandArguments);
  }
}
