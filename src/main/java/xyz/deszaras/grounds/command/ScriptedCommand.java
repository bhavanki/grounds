package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptCallable;
import xyz.deszaras.grounds.script.ScriptFactory;
import xyz.deszaras.grounds.script.ScriptFactoryException;

/**
 * Runs a scripted command.<p>
 *
 * A scripted command is defined in an attribute of an extension
 * in the universe of the player running it. The name of the
 * attribute matches the name of the command and must start with a
 * '$' character. See {@link ScriptFactory#newScript(Attr)} for
 * details on the necessary attribute structure.<p>
 *
 * A player may call a script if they pass the USE category on
 * the extension where the script is defined.
 */
public class ScriptedCommand extends Command<String> {

  private final Extension scriptExtension;
  private final Script script;
  private final List<String> scriptArguments;

  /**
   * Creates a new scripted command.
   *
   * @param actor actor executing the command
   * @param player player executing the command
   * @param scriptExtension extension where the script is defined
   * @param script script to run
   * @param scriptArguments arguments to pass to the script
   */
  public ScriptedCommand(Actor actor, Player player, Extension scriptExtension,
                         Script script, List<String> scriptArguments) {
    super(actor, player);
    this.scriptExtension = scriptExtension;
    this.script = script;
    this.scriptArguments = ImmutableList.copyOf(scriptArguments);
  }

  @Override
  public String execute() {

    if (!scriptExtension.passes(Category.USE, player)) {
      actor.sendMessage("You do not have permission to use this command");
      return null;
    }

    return new ScriptCallable(actor, player, script, scriptArguments).call();
  }

  /**
   * Creates a new scripted command from a script attribute and arguments. Note
   * that this method does not have the signature expected by
   * {@link CommandFactory}.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param scriptExtension script extension
   * @param scriptAttr attribute defining the script to run
   * @param scriptArguments unresolved script arguments
   * @return scripted command
   * @throws CommandFactoryException if the command cannot be built
   */
  public static ScriptedCommand newCommand(Actor actor, Player player, Extension scriptExtension,
                                           Attr scriptAttr, List<String> scriptArguments)
      throws CommandFactoryException {

    try {
      // Build a script from the command attribute.
      Optional<Thing> scriptOwner = scriptExtension.getOwner();
      if (scriptOwner.isEmpty()) {
        throw new CommandFactoryException("Cannot build command from script, extension " +
                                          scriptExtension.getName() + " has no owner");
      }
      Thing scriptOwnerThing = scriptOwner.get();
      if (!(scriptOwnerThing instanceof Player)) {
        throw new CommandFactoryException("Cannot build command from script, extension " +
                                          scriptExtension.getName() + " has a non-player owner");
      }
      Script script = new ScriptFactory().newScript(scriptAttr, (Player) scriptOwnerThing, scriptExtension);

      return new ScriptedCommand(actor, player, scriptExtension, script, scriptArguments);
    } catch (ScriptFactoryException e) {
      throw new CommandFactoryException("Failed to build script", e);
    }
  }
}
