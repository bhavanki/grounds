package xyz.deszaras.grounds.server;

import java.io.Console;
import java.util.Optional;
import java.util.concurrent.Future;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandExecutor.CommandResult;
import xyz.deszaras.grounds.command.CommandFactory;
import xyz.deszaras.grounds.command.ExitCommand;
import xyz.deszaras.grounds.model.Player;

public class SingleUser {

  public static void main(String[] args) throws Exception {
    Console console = System.console();
    if (console == null) {
      throw new IllegalStateException("No console!");
    }

    Actor actor = new Actor();
    Player player = Player.GOD;
    CommandFactory commandFactory = new CommandFactory();
    CommandExecutor commandExecutor = new CommandExecutor(commandFactory);

    console.printf("Welcome to Grounds.\n");
    console.printf("This is single-user mode. Use ^D or 'exit' to quit.\n\n");
    CommandResult lastCommandResult = new CommandResult(true, null);

    try {
      while (true) {
        console.printf(lastCommandResult.isSuccessful() ? "√ " : "X ");
        console.printf("# ");
        String line = console.readLine();
        if (line == null) {
          break;
        }

        Future<CommandResult> commandFuture = commandExecutor.submit(actor, player, line);
        lastCommandResult = commandFuture.get();

        if (!lastCommandResult.isSuccessful()) {
          Optional<CommandException> commandException = lastCommandResult.getCommandException();
          if (commandException.isPresent()) {
            console.printf("ERROR: %s\n", commandException.get().getMessage());
          }
        }

        if (lastCommandResult.getCommandClass().isPresent() &&
            lastCommandResult.getCommandClass().get().equals(ExitCommand.class)) {
          break;
        }

        String message;
        boolean wroteSeparator = false;
        while ((message = actor.getNextMessage()) != null) {
          if (!wroteSeparator) {
            console.printf("========================================\n");
            wroteSeparator = true;
          }
          console.printf("%s\n", message);
        }
      }
    } finally {
      commandExecutor.shutdown();
    }

    console.printf("\n\nBye!\n");
  }

}
