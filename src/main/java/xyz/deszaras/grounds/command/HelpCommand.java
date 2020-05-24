package xyz.deszaras.grounds.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;

public class HelpCommand extends Command<Boolean> {

  private final String commandName;

  public HelpCommand(Actor actor, Player player, String commandName) {
    super(actor, player);
    this.commandName = Objects.requireNonNull(commandName);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (commandName.equalsIgnoreCase("commands")) {
      List<String> commandNames =
          new ArrayList<>(CommandExecutor.INSTANCE.getCommandFactory().getCommandNames());
      Collections.sort(commandNames);
      for (String commandName : commandNames) {
        actor.sendMessage(commandName);
      }
      return true;
    }

    Class<? extends Command> commandClass =
        CommandExecutor.INSTANCE.getCommandFactory().getCommandClass(commandName);
    if (commandClass == null) {
      throw new CommandException("Unrecognized command " + commandName);
    }

    try {
      Method helpMethod = commandClass.getMethod("help");
      String helpText = (String) helpMethod.invoke(null);
      actor.sendMessage(helpText);
      return true;
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

  public static String help() {
    return "HELP [<command> | commands]\n\n" +
        "Gets help for a command\n\n" +
        "`HELP commands` lists all available commands";
  }
}
