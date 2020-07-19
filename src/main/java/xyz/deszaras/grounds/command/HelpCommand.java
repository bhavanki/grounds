package xyz.deszaras.grounds.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.fusesource.jansi.Ansi;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptFactoryException;
import xyz.deszaras.grounds.util.AnsiUtils;

@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class HelpCommand extends Command<String> {

  private static final ResourceBundle helpResource =
      ResourceBundle.getBundle("bundles.help.Help", Locale.getDefault());

  private final String commandName;

  public HelpCommand(Actor actor, Player player, String commandName) {
    super(actor, player);
    this.commandName = Objects.requireNonNull(commandName);
  }

  private static final String HELP_FORMAT = "%s\n\n%s\n\n%s";

  @Override
  protected String executeImpl() throws CommandException {
    if (commandName.equalsIgnoreCase("commands")) {
      List<String> commandNames =
          new ArrayList<>(CommandExecutor.getInstance().getCommandFactory().getCommandNames());
      Collections.sort(commandNames);
      return commandNames.stream().collect(Collectors.joining("\n"));
    }

    String upperCommandName = commandName.toUpperCase();

    ResourceBundle bundle;
    if (commandName.startsWith("$")) {
      String baseCommandName = commandName.replaceAll("_.*", "");
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
    ensureMinArgs(commandArgs, 1);
    String commandName = String.join("_", commandArgs);
    return new HelpCommand(actor, player, commandName);
  }
}
