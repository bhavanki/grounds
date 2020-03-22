package xyz.deszaras.grounds.command;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import xyz.deszaras.grounds.model.Player;

/**
 * A factory for commands. This factory is responsible for constructing
 * command objects from tokenized command lines.<p>
 *
 * In order for this factory to build a command, its command class must
 * contain a static {@code newCommand()} method delegate to.
 */
public class CommandFactory {

  private static final Map<String, Class<? extends Command>> COMMANDS;

  static {
    COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("LOOK", LookCommand.class)
        .put("L", LookCommand.class)
        .put("INSPECT", InspectCommand.class)
        .put("TELEPORT", TeleportCommand.class)
        .put("TP", TeleportCommand.class)
        .put("MOVE", MoveCommand.class)
        .put("GO", MoveCommand.class)
        .put("G", MoveCommand.class)
        .put("BUILD", BuildCommand.class)
        .put("SET_ATTR", SetAttrCommand.class)
        .put("REMOVE_ATTR", RemoveAttrCommand.class)
        .put("CLAIM", ClaimCommand.class)
        .put("ABANDON", AbandonCommand.class)
        .put("LOAD", LoadCommand.class)
        .put("SAVE", SaveCommand.class)
        .put("SWITCH_PLAYER", SwitchPlayerCommand.class)
        .put("EXIT", ExitCommand.class)
        .put("HASH_PASSWORD", HashPasswordCommand.class)
        .put("INDEX", IndexCommand.class)
        .put("ROLE", RoleCommand.class)
        .put("ACTOR", ActorCommand.class)
        .put("SHUTDOWN", ShutdownCommand.class)
        .build();
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
    String commandName = line.get(0).toUpperCase();
    List<String> commandArgs = line.subList(1, line.size());

    Class<? extends Command> commandClass = COMMANDS.get(commandName);
    if (commandClass == null) {
      throw new CommandFactoryException("Unrecognized command " + commandName);
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
