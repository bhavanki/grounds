package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import xyz.deszaras.grounds.model.Attr;
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

  /**
   * Creates a new scripted command from a script attribute and arguments. Note
   * that this method does not have the signature expected by
   * {@link CommandFactory}.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param scriptAttr attribute defining the script to run
   * @param scriptArguments unresolved script arguments
   * @return scripted command
   * @throws CommandFactoryException if the command cannot be built
   */
  public static ScriptedCommand newCommand(Actor actor, Player player, Attr scriptAttr,
                                           List<String> scriptArguments)
      throws CommandFactoryException {
    try {
      // Build a script from the command attribute.
      Script script = new ScriptFactory().newScript(scriptAttr);

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
