package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.api.PluginCall;
import xyz.deszaras.grounds.api.PluginCallFactoryException;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
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

  private static final String HELP_FORMAT =
      AnsiUtils.color("%s", Ansi.Color.CYAN, false) + "\n\n" +
      "%s\n" +
      AnsiUtils.color("{hr -}", Ansi.Color.CYAN, false) + "\n\n" +
      "%s\n\n" +
      AnsiUtils.color("{hr -}", Ansi.Color.CYAN, false) + "\n" +
      AnsiUtils.color("Roles: %s", Ansi.Color.CYAN, false);

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

      try {
        PluginCall pluginCall =
            CommandExecutor.getInstance().getCommandFactory().findPluginCall(baseCommandName)
            .orElseThrow(() -> new CommandException("Plugin call command " + baseCommandName +
                                                    " not found"));
        bundle = pluginCall.getHelpBundle();
      } catch (PluginCallFactoryException e) {
        throw new CommandException("Could not build plugin call for command " + baseCommandName +
                                   " in order to get help text", e);
      }
    } else {
      bundle = helpResource;
    }

    if (bundle.containsKey(upperCommandName + ".syntax")) {
      String roles = "...";
      if (bundle.containsKey(upperCommandName + ".roles")) {
        roles = bundle.getString(upperCommandName + ".roles");
        switch (roles) {
        case "ALL":
          roles = "GUEST, DENIZEN, BARD, ADEPT, THAUMATURGE";
          break;
        case "NONGUEST":
          roles = "DENIZEN, BARD, ADEPT, THAUMATURGE";
          break;
        case "WIZARD":
          roles = "BARD, ADEPT, THAUMATURGE";
          break;
        case "NONE":
          roles = "GOD only";
          break;
        default:
          roles = roles.replace(",", ", ");
          break;
        }
      }
      return String.format(HELP_FORMAT,
                           bundle.getString(upperCommandName + ".syntax"),
                           bundle.getString(upperCommandName + ".summary"),
                           bundle.getString(upperCommandName + ".description"),
                           roles);
    }

    throw new CommandException("Help is not available for " + upperCommandName);
  }

  public static HelpCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    return new HelpCommand(actor, player, commandArgs);
  }
}
