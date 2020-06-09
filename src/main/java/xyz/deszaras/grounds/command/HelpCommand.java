package xyz.deszaras.grounds.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.fusesource.jansi.Ansi;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiUtils;

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
  public String execute() throws CommandException {
    if (commandName.equalsIgnoreCase("commands")) {
      List<String> commandNames =
          new ArrayList<>(CommandExecutor.getInstance().getCommandFactory().getCommandNames());
      Collections.sort(commandNames);
      return commandNames.stream().collect(Collectors.joining("\n"));
    }

    String upperCommandName = commandName.toUpperCase();
    if (helpResource.containsKey(upperCommandName + ".syntax")) {
      String syntax = AnsiUtils.color(helpResource.getString(upperCommandName + ".syntax"),
                                      Ansi.Color.CYAN, false);
      return String.format(HELP_FORMAT,
                           syntax,
                           helpResource.getString(upperCommandName + ".summary"),
                           helpResource.getString(upperCommandName + ".description"));
    }

    Class<? extends Command> commandClass =
        CommandExecutor.getInstance().getCommandFactory().getCommandClass(commandName);
    if (commandClass == null) {
      throw new CommandException("Unrecognized command " + commandName);
    }

    try {
      Method helpMethod = commandClass.getMethod("help");
      return (String) helpMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new CommandException("Command class " + commandClass.getName() +
                                 " lacks a static help method!");
    } catch (IllegalAccessException e) {
      throw new CommandException("Failed to get help text: " + e.getMessage());
    } catch (InvocationTargetException e) {
      throw new CommandException("Failed to get help text: " + e.getCause().getMessage());
    }
 }

  public static HelpCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String commandName = commandArgs.get(0);
    return new HelpCommand(actor, player, commandName);
  }
}
