package xyz.deszaras.grounds.command;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import xyz.deszaras.grounds.model.Player;

public class CommandExecutor {

  public static final CommandExecutor INSTANCE =
      new CommandExecutor(new CommandFactory());

  private final CommandFactory commandFactory;
  private final ExecutorService commandExecutorService;

  private CommandExecutor(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
    commandExecutorService = Executors.newSingleThreadExecutor();
  }

  public Future<CommandResult> submit(Actor actor, Player player, String commandLine) {
    CommandCallable callable = new CommandCallable(actor, player, commandLine);
    return commandExecutorService.submit(callable);
  }

  public void shutdown() {
    commandExecutorService.shutdown();
  }

  public static class CommandResult {
    private final boolean success;
    private final CommandException commandException;
    private final Class<? extends Command> commandClass;

    public CommandResult(boolean success, Class<? extends Command> commandClass) {
      this.success = success;
      commandException = null;
      this.commandClass = commandClass;
    }

    public CommandResult(CommandException e) {
      success = false;
      commandException = e;
      this.commandClass = null;
    }

    public boolean isSuccessful() {
      return success;
    }

    public Optional<CommandException> getCommandException() {
      return Optional.ofNullable(commandException);
    }

    public Optional<Class<? extends Command>> getCommandClass() {
      return Optional.ofNullable(commandClass);
    }
  }

  private class CommandCallable implements Callable<CommandResult> {

    private final Actor actor;
    private final Player player;
    private final String commandLine;

    private CommandCallable(Actor actor, Player player, String commandLine) {
      this.actor = actor;
      this.player = player;
      this.commandLine = commandLine;
    }

    @Override
    public CommandResult call() {
      try {
        Command command =
            commandFactory.getCommand(actor, player, commandLine);
        return new CommandResult(command.execute(), command.getClass());
      } catch (CommandException e) {
        return new CommandResult(e);
      }
    }
  }
}
