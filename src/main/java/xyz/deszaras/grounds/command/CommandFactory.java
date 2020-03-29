package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xyz.deszaras.grounds.model.Player;

/**
 * A factory for commands. This factory is responsible for constructing
 * command objects from tokenized command lines.<p>
 *
 * In order for this factory to build a command, its command class must
 * contain a static {@code newCommand()} method delegate to.
 */
public class CommandFactory {

  private final Map<String, Class<? extends Command>> commands;

  public CommandFactory(Map<String, Class<? extends Command>> commands) {
    this.commands = ImmutableMap.copyOf(commands);
  }

  /**
   * Gets the class of the command that implements the given command
   * name.
   *
   * @param commandName command name
   * @return supported command, or null if unsupported
   */
  public Class<? extends Command> getCommandClass(String commandName) {
    if (commandName.startsWith("$")) { // minor wart
      return ScriptedCommand.class;
    }
    return commands.get(commandName.toUpperCase());
  }

  /**
   * Gets all of the command names supported by this factory.
   *
   * @return supported command names
   */
  public Set<String> getCommandNames() {
    return commands.keySet();
  }

  /**
   * Gets a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param line command line entered in the shell
   * @throws CommandFactoryException if the command cannot be built
   */
  public Command getCommand(Actor actor, Player player, List<String> line)
      throws CommandFactoryException {
    if (line.isEmpty()) {
      return new NoOpCommand(actor, player);
    }
    String commandName = line.get(0);

    Class<? extends Command> commandClass = getCommandClass(commandName);
    if (commandClass == null) {
      throw new CommandFactoryException("Unrecognized command " + commandName);
    }

    // For scripted commands, the command name needs to be passed along
    // with everything else.
    List<String> commandArgs;
    if (commandClass.equals(ScriptedCommand.class)) {
      commandArgs = line;
    } else {
      commandArgs = line.subList(1, line.size());
    }

    try {
      Method newCommandMethod =
          commandClass.getMethod("newCommand", Actor.class, Player.class, List.class);
      return (Command) newCommandMethod.invoke(null, actor, player, commandArgs);
    } catch (NoSuchMethodException e) {
      throw new CommandFactoryException("Command class " + commandClass.getName() +
                                 " lacks a static newCommand method!");
    } catch (IllegalAccessException e) {
      throw new CommandFactoryException("Failed to create new command", e);
    } catch (InvocationTargetException e) {
      throw new CommandFactoryException("Failed to create new command", e.getCause());
    }
  }
}
