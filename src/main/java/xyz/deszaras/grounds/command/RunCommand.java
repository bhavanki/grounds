package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.CommandLineUtils;

/**
 * Runs commands from a file.<p>
 *
 * Arguments: command file<br>
 * Checks: player is GOD
 */
public class RunCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(RunCommand.class);

  private final File f;

  public RunCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  public Boolean execute() throws CommandException {
    if (!player.equals(Player.GOD)) {
      throw new CommandException("Only GOD can run commands from a file");
    }

    CommandFactory commandFactory = CommandExecutor.getInstance().getCommandFactory();

    List<String> commandLines;
    try (Stream<String> lineStream = Files.lines(f.toPath())) {
      commandLines = lineStream.collect(Collectors.toList());
    } catch (IOException e) {
      throw new CommandException("Failed to read command file: " + e.getMessage());
    }

    for (String line : commandLines) {
      if (line.startsWith("#") || line.startsWith("//")) {
        continue;
      }
      List<String> tokens = CommandLineUtils.tokenize(line);
      if (tokens.isEmpty()) {
        continue;
      }

      player.sendMessage(new Message(player, Message.Style.INFO,
                                     String.format("Running command:\n%s", line)));
      CommandCallable commandCallable =
          new CommandCallable(actor, player, tokens, commandFactory);
      CommandResult commandResult = commandCallable.call();

      // TBD: refactor with Shell
      if (!commandResult.isSuccessful()) {
        Optional<CommandException> commandException =
            commandResult.getCommandException();
        if (commandException.isPresent()) {
          player.sendMessage(new Message(player, Message.Style.COMMAND_EXCEPTION,
                                         commandException.get().getMessage()));
          return false;
        }
        Optional<CommandFactoryException> commandFactoryException =
            commandResult.getCommandFactoryException();
        if (commandFactoryException.isPresent()) {
          player.sendMessage(new Message(player, Message.Style.COMMAND_EXCEPTION,
                                         commandFactoryException.get().getMessage()));
          return false;
        }

        LOG.warn("Ran command which failed with exception: " + line);
        return false;
      } else {
        Object result = commandResult.getResult();
        if (result != null && !(result instanceof Boolean)) {
          player.sendMessage(new Message(player, Message.Style.INFO, result.toString()));
        }
        Optional<Class<? extends Command>> commandClass = commandResult.getCommandClass();
        if (commandClass.isPresent() &&
          // Problem: Need to catch shutdown command before it's executed!
            (commandClass.get().equals(ExitCommand.class) ||
             commandClass.get().equals(SwitchPlayerCommand.class))) {
          player.sendMessage(new Message(player, Message.Style.INFO,
                                         "Ignoring command of type " + commandClass.get().getSimpleName()));
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
