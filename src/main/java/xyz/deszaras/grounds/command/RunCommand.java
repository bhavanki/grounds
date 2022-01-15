package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jline.reader.impl.DefaultParser;

import xyz.deszaras.grounds.model.Player;

/**
 * Runs commands from a file.<p>
 *
 * Arguments: command file
 */
@PermittedRoles(roles = {})
public class RunCommand extends Command<Boolean> {

  private final File f;

  public RunCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    List<String> commandLines;
    try (Stream<String> lineStream = Files.lines(f.toPath())) {
      commandLines = lineStream.collect(Collectors.toList());
    } catch (IOException e) {
      throw new CommandException("Failed to read command file: " + e.getMessage());
    }

    DefaultParser parser = new DefaultParser();
    for (String line : commandLines) {
      if (line.startsWith("#") || line.startsWith("//")) {
        continue;
      }
      List<String> tokens = parser.parse(line, line.length()).words();
      if (tokens.isEmpty() || String.join("", tokens).isEmpty()) {
        continue;
      }

      player.sendMessage(new Message(player, Message.Style.INFO,
                                     String.format("Running command:\n%s", line)));
      CommandCallable commandCallable =
          new CommandCallable(actor, player, tokens, CommandExecutor.getInstance());
      CommandResult commandResult = commandCallable.call();

      if (!commandResult.isSuccessful()) {
        player.sendMessage(commandResult.getFailureMessage(player));
        return false;
      } else {
        Object result = commandResult.getResult();
        player.sendMessage(new Message(player, Message.Style.INFO,
                                       result != null ? result.toString() : "<null>"));
        Optional<Command> command = commandResult.getCommand();
        if (command.isPresent() &&
          // Problem: Need to catch shutdown command before it's executed!
            (command.get() instanceof ExitCommand) ||
             command.get() instanceof SwitchPlayerCommand) {
          player.sendMessage(new Message(player, Message.Style.INFO,
                                         "Ignoring command of type " +
                                         command.get().getClass().getSimpleName()));
        }
      }
    }

    return true;
  }

  public static RunCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return new RunCommand(actor, player, new File(commandArgs.get(0)));
  }
}
