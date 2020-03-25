package xyz.deszaras.grounds.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Player;

public class HelpCommand extends Command {

  private final String commandName;

  public HelpCommand(Actor actor, Player player, String commandName) {
    super(actor, player);
    this.commandName = Objects.requireNonNull(commandName);
  }

  @Override
  public boolean execute() {
    Class<? extends Command> commandClass =
        CommandFactory.getCommandClass(commandName);
    if (commandClass == null) {
      actor.sendMessage("Unrecognized command " + commandName);
      return false;
    }

    try {
      Method helpMethod = commandClass.getMethod("help");
      String helpText = (String) helpMethod.invoke(null);
      actor.sendMessage(helpText);
      return true;
    } catch (NoSuchMethodException e) {
      actor.sendMessage("Command class " + commandClass.getName() +
                        " lacks a static help method!");
    } catch (IllegalAccessException e) {
      actor.sendMessage("Failed to get help text: " + e.getMessage());
    } catch (InvocationTargetException e) {
      actor.sendMessage("Failed to get help text: " + e.getCause().getMessage());
    }
    return false;
 }

  public static HelpCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String commandName = commandArgs.get(0);
    return new HelpCommand(actor, player, commandName);
  }

  public static String help() {
    return "HELP <command>\n\n" +
        "Gets help for a command";
  }
}
