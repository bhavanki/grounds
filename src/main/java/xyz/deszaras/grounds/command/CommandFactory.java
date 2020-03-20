package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.deszaras.grounds.model.Player;

public class CommandFactory {

  // https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
  // This doesn't obey escaped quotes, though.
  private static final Pattern TOKENIZE_PATTERN =
      Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  @VisibleForTesting
  static List<String> tokenize(String line) {
    List<String> tokens = new ArrayList<>();
    Matcher m = TOKENIZE_PATTERN.matcher(line);
    while (m.find()) {
      if (m.group(1) != null) {
        // quotation marks
        tokens.add(m.group(1));
      } else if (m.group(2) != null) {
        // apostrophes
        tokens.add(m.group(2));
      } else {
        tokens.add(m.group());
      }
    }
    return tokens;
  }

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
        .put("LOAD", LoadCommand.class)
        .put("SAVE", SaveCommand.class)
        .put("EXIT", ExitCommand.class)
        .put("SHUTDOWN", ShutdownCommand.class)
        .build();
  }

  public Command getCommand(Actor actor, Player player, String line)
      throws CommandException {
    if (line.trim().equals("")) {
      return new NoOpCommand(actor, player);
    }
    List<String> tokens = tokenize(line);
    String commandName = tokens.get(0).toUpperCase();
    List<String> commandArgs = tokens.subList(1, tokens.size());

    Class<? extends Command> commandClass = COMMANDS.get(commandName);
    if (commandClass == null) {
      throw new CommandException("Unrecognized command " + commandName);
    }

    try {
      Method newCommandMethod =
          commandClass.getMethod("newCommand", Actor.class, Player.class, List.class);
      return (Command) newCommandMethod.invoke(null, actor, player, commandArgs);
    } catch (NoSuchMethodException e) {
      throw new CommandException("Command class " + commandClass.getName() +
                                 " lacks a static newCommand method!");
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new CommandException("Failed to create new command", e);
    }
  }
}
