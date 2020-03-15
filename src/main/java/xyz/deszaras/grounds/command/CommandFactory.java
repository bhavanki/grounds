package xyz.deszaras.grounds.command;

import com.google.common.base.Splitter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class CommandFactory {

  // https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
  // This doesn't obey escaped quotes, though.
  private static final Pattern TOKENIZE_PATTERN =
      Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  private static List<String> tokenize(String line) {
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

  public Optional<Command> getCommand(Actor actor, Player player, String line)
      throws CommandException {
    if (line.trim().equals("")) {
      return Optional.of(new NoOpCommand(actor, player));
    }
    List<String> tokens = tokenize(line);
    String commandName = tokens.get(0).toUpperCase();
    List<String> commandArgs = tokens.subList(1, tokens.size());

    switch (commandName) {
      case "LOOK":
        return Optional.of(new LookCommand(actor, player));
      case "MOVE":
        ensureMinArgs(commandArgs, 1);
        Optional<Thing> destination = Multiverse.MULTIVERSE.findThing(commandArgs.get(0));
        if (!destination.isPresent()) {
          throw new CommandException("Failed to find destination in universe");
        }
        return Optional.of(new MoveCommand(actor, player, (Place) destination.get()));
      case "BUILD":
        ensureMinArgs(commandArgs, 2);
        String type = commandArgs.get(0);
        String name = commandArgs.get(1);
        List<String> buildArgs = commandArgs.subList(2, commandArgs.size());
        return Optional.of(new BuildCommand(actor, player, type, name, buildArgs));
      case "LOAD":
        ensureMinArgs(commandArgs, 1);
        return Optional.of(new LoadCommand(actor, player, new File(commandArgs.get(0))));
      case "SAVE":
        ensureMinArgs(commandArgs, 1);
        return Optional.of(new SaveCommand(actor, player, new File(commandArgs.get(0))));
      default:
        return Optional.empty();
    }
  }

  private static void ensureMinArgs(List<String> l, int n) throws CommandException {
    if (l.size() < n) {
      throw new CommandException("Need at least " + n + " arguments, got " + l.size());
    }
  }
}
