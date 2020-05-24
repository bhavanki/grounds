package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * A factory for commands. This factory is responsible for constructing
 * command objects from tokenized command lines.<p>
 *
 * In order for this factory to build a command, its command class must
 * contain a static {@code newCommand()} method to delegate to.
 */
public class CommandFactory {

  private final Map<String, Class<? extends Command>> commands;

  /**
   * Creates a new command factory.
   *
   * @param  commands map of command classes to support, keyed by command name
   */
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
   * Gets an instance of a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param line command line entered in the shell
   * @return command
   * @throws CommandFactoryException if the command cannot be built
   */
  public Command getCommand(Actor actor, Player player, List<String> line)
      throws CommandFactoryException {
    if (line.isEmpty()) {
      return new NoOpCommand(actor, player);
    }
    String commandName = line.get(0);

    if (commandName.startsWith("$")) {
      return newScriptedCommand(actor, player, line);
    }

    Class<? extends Command> commandClass = getCommandClass(commandName);
    if (commandClass == null) {
      throw new CommandFactoryException("Unrecognized command " + commandName);
    }
    List<String> commandArgs = line.subList(1, line.size());

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

  /**
   * Gets a scripted command. This method hunts for the script attribute
   * defining the command among all extensions in the player's universe.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param line command line entered in the shell
   * @return scripted command
   * @throws CommandFactoryException if the command cannot be built
   */
  private ScriptedCommand newScriptedCommand(Actor actor, Player player, List<String> line)
      throws CommandFactoryException {
    String commandName = line.get(0);
    List<String> scriptArguments = line.subList(1, line.size());

    // Find the attribute defining the command. It must be attached
    // to an extension in the player's universe and have an attribute
    // list as a value.
    Optional<Attr> scriptAttr = Optional.empty();
    // this is inefficient :(
    for (Extension extension : player.getUniverse().getThings(Extension.class)) {
      scriptAttr = extension.getAttr(commandName, Attr.Type.ATTRLIST);
      if (scriptAttr.isPresent()) {
        break;
      }
    }
    if (scriptAttr.isEmpty()) {
      throw new CommandFactoryException("Failed to locate script attribute for command " + commandName);
    }

    return ScriptedCommand.newCommand(actor, player, scriptAttr.get(), scriptArguments);
  }
}
