package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.api.PluginCall;
import xyz.deszaras.grounds.api.PluginCallFactoryException;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptFactoryException;
import xyz.deszaras.grounds.util.AnsiUtils;

@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class HelpCommand extends Command<String> {

  private static final ResourceBundle helpResource =
      ResourceBundle.getBundle("bundles.help.Help", Locale.getDefault());

  private final List<String> args;

  public HelpCommand(Actor actor, Player player, List<String> args) {
    super(actor, player);
    this.args = ImmutableList.copyOf(args);
  }

  private static final String COMMANDS_LIST;

  static {
    List<String> commandNames =
      new ArrayList<>(CommandExecutor.COMMANDS.keySet());
    Collections.sort(commandNames);
    COMMANDS_LIST = String.join("\n", commandNames);
  }

  private static final String HELP_FORMAT = "%s\n\n%s\n\n%s";

  @Override
  protected String executeImpl() throws CommandException {
    if (args.isEmpty()) {
      return helpResource.getString("_MAIN.content");
    }

    String commandName = String.join("_", args);
    if (commandName.equalsIgnoreCase("commands")) {
      return COMMANDS_LIST;
    }

    String upperCommandName = commandName.toUpperCase();

    ResourceBundle bundle = null;
    if (commandName.startsWith("$")) {
      String baseCommandName = commandName.replaceAll("_.*", "");

      // In progress replacement of scripts with plugins.
      try {
        Optional<PluginCall> pluginCall =
            CommandExecutor.getInstance().getCommandFactory().findPluginCall(baseCommandName);
        if (pluginCall.isPresent()) {
          bundle = pluginCall.get().getHelpBundle();
        }
      } catch (PluginCallFactoryException e) {
        throw new CommandException("Could not build plugin call for command " + baseCommandName +
                                   " in order to get help text", e);
      }

      if (bundle == null) {
        try {
          Script commandScript =
              CommandExecutor.getInstance().getCommandFactory().findScript(baseCommandName)
              .orElseThrow(() -> new CommandException("Scripted command " + baseCommandName +
                                                      " not found"));

          bundle = commandScript.getHelpBundle();
        } catch (ScriptFactoryException e) {
          throw new CommandException("Could not build script for command " + baseCommandName +
                                     " in order to get help text", e);
        }
      }
    } else {
      bundle = helpResource;
    }

    if (bundle.containsKey(upperCommandName + ".syntax")) {
      String syntax = AnsiUtils.color(bundle.getString(upperCommandName + ".syntax"),
                                      Ansi.Color.CYAN, false);
      return String.format(HELP_FORMAT,
                           syntax,
                           bundle.getString(upperCommandName + ".summary"),
                           bundle.getString(upperCommandName + ".description"));
    }

    throw new CommandException("Help is not available for " + upperCommandName);
  }

  public static HelpCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    return new HelpCommand(actor, player, commandArgs);
  }
}
