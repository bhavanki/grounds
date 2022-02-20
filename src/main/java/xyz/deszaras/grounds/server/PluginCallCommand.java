package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;

import java.util.List;

import xyz.deszaras.grounds.api.PluginCall;
import xyz.deszaras.grounds.api.PluginCallable;
import xyz.deszaras.grounds.api.PluginCallFactory;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * Runs a plugin call command.<p>
 *
 * A plugin call command is defined in an attribute of an extension.
 * The name of the attribute matches the name of the command and
 * must start with a '$' character. See
 * {@link PluginCallFactory#newScript(Attr)} for details on the
 * necessary attribute structure.<p>
 *
 * A player may call a plugin if they pass the USE category on
 * the extension where the plugin call is defined.
 */
@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class PluginCallCommand extends Command<String> {

  private final Extension pluginCallExtension;
  private final PluginCall pluginCall;
  private final List<String> pluginCallArguments;

  /**
   * Creates a new plugin call command.
   *
   * @param actor               actor executing the command
   * @param player              player executing the command
   * @param pluginCall          plugin call to execute
   * @param pluginCallArguments arguments to pass to the plugin call
   */
  public PluginCallCommand(Actor actor, Player player, PluginCall pluginCall,
                           List<String> pluginCallArguments) {
    super(actor, player);
    this.pluginCall = pluginCall;
    this.pluginCallExtension = pluginCall.getExtension();
    this.pluginCallArguments = ImmutableList.copyOf(pluginCallArguments);
  }

  @Override
  protected String executeImpl() throws CommandException {

    if (!pluginCallExtension.passes(Category.USE, player)) {
      throw new PermissionException("Permission denied");
    }

    return new PluginCallable(actor, player, pluginCall, pluginCallArguments).call();
  }

  /**
   * Creates a new plugin call command from a plugin call attribute and
   * arguments. Note that this method does not have the signature expected by
   * {@link CommandFactory}.
   *
   * @param actor               actor submitting the command
   * @param player              player currently assumed by the actor
   * @param pluginCall          plugin call to execute
   * @param pluginCallArguments unresolved plugin call arguments
   * @return plugin call command
   * @throws CommandFactoryException if the command cannot be built
   */
  public static PluginCallCommand newCommand(Actor actor, Player player,
                                             PluginCall pluginCall,
                                             List<String> pluginCallArguments)
      throws CommandFactoryException {
    return new PluginCallCommand(actor, player, pluginCall, pluginCallArguments);
  }
}
