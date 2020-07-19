package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;

import java.util.List;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptCallable;
import xyz.deszaras.grounds.script.ScriptFactory;

/**
 * Runs a scripted command.<p>
 *
 * A scripted command is defined in an attribute of an extension.
 * The name of the attribute matches the name of the command and
 * must start with a '$' character. See
 * {@link ScriptFactory#newScript(Attr)} for details on the
 * necessary attribute structure.<p>
 *
 * A player may call a script if they pass the USE category on
 * the extension where the script is defined.
 */
@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class ScriptedCommand extends Command<String> {

  private final Extension scriptExtension;
  private final Script script;
  private final List<String> scriptArguments;

  /**
   * Creates a new scripted command.
   *
   * @param actor actor executing the command
   * @param player player executing the command
   * @param script script to run
   * @param scriptArguments arguments to pass to the script
   */
  public ScriptedCommand(Actor actor, Player player, Script script,
                         List<String> scriptArguments) {
    super(actor, player);
    this.script = script;
    this.scriptExtension = script.getExtension();
    this.scriptArguments = ImmutableList.copyOf(scriptArguments);
  }

  @Override
  protected String executeImpl() throws CommandException {

    if (!scriptExtension.passes(Category.USE, player)) {
      throw new CommandException("You do not have permission to run the script");
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
   * @param script the script to run
   * @param scriptArguments unresolved script arguments
   * @return scripted command
   * @throws CommandFactoryException if the command cannot be built
   */
  public static ScriptedCommand newCommand(Actor actor, Player player, Script script,
                                           List<String> scriptArguments)
      throws CommandFactoryException {
    return new ScriptedCommand(actor, player, script, scriptArguments);
  }
}
