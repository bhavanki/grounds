package xyz.deszaras.grounds.server;

import java.io.Console;
import java.util.Optional;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
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

    console.printf("Welcome to Grounds.\n");
    console.printf("This is single-user mode. Use ^D or 'exit' to quit.\n\n");
    boolean lastCommandResult = true;

    while (true) {
      console.printf(lastCommandResult ? "√ " : "X ");
      console.printf("# ");
      String line = console.readLine();
      if (line == null) {
        break;
      }

      try {
        Optional<Command> command = commandFactory.getCommand(actor, player, line);
        if (!command.isPresent()) {
          console.printf("What?\n");
          lastCommandResult = false;
        } else {
          if (command.get() instanceof ExitCommand) {
            break;
          }
          lastCommandResult = command.get().execute();
        }
      } catch (CommandException e) {
        console.printf("ERROR: %s\n", e.getMessage());
        lastCommandResult = false;
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

    console.printf("\n\nBye!\n");
  }

}
