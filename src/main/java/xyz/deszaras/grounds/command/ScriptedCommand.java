package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptCallable;
import xyz.deszaras.grounds.script.ScriptFactory;
import xyz.deszaras.grounds.script.ScriptFactoryException;
import xyz.deszaras.grounds.util.ArgumentResolverException;

/**
 * Runs a scripted command.<p>
 *
 * A scripted command is defined in an attribute of an extension
 * in the universe of the player running it. The name of the
 * attribute matches the name of the command and must start with a
 * '$' character. See {@link ScriptFactory#newScript(Attr)} for
 * details on the necessary attribute structure.
 */
public class ScriptedCommand extends Command {

  private final Script script;
  private final List<Object> scriptArguments;

  /**
   * Creates a new scripted command.
   *
   * @param actor actor executing the command
   * @param player player executing the command
   * @param script script to run
   * @param scriptArguments arguments to pass to the script
   */
  public ScriptedCommand(Actor actor, Player player, Script script,
                         List<Object> scriptArguments) {
    super(actor, player);
    this.script = script;
    this.scriptArguments = ImmutableList.copyOf(scriptArguments);
  }

  @Override
  public boolean execute() {
    return new ScriptCallable(player, script, scriptArguments).call();
  }

  public static ScriptedCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String commandName = commandArgs.get(0);
    List<String> scriptArguments =
      commandArgs.subList(1, commandArgs.size());

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

    try {
      // Build a script from the command attribute.
      Script script = new ScriptFactory().newScript(commandAttr.get());

      // Resolve the script arguments.
      List<Object> resolvedArguments =
          script.resolveScriptArguments(scriptArguments, player);

      // TBD: policy ???

      return new ScriptedCommand(actor, player, script, resolvedArguments);
    } catch (ArgumentResolverException e) {
      throw new CommandFactoryException("Failed to resolve script arguments", e);
    } catch (ScriptFactoryException e) {
      throw new CommandFactoryException("Failed to build script", e);
    }
  }
}
