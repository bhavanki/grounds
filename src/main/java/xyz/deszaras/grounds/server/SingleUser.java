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

    while (true) {
      console.printf("# ");
      String line = console.readLine();
      if (line == null) {
        break;
      }

      try {
        Optional<Command> command = commandFactory.getCommand(actor, player, line);
        if (!command.isPresent()) {
          console.printf("What?\n");
        } else {
          if (command instanceof ExitCommand) {
            break;
          }
          boolean result = command.get().execute();
          console.printf("Command result: " + result + "\n");
        }
      } catch (CommandException e) {
        console.printf("ERROR: %s\n", e.getMessage());
      }

      String message;
      while ((message = actor.getNextMessage()) != null) {
        console.printf("Message: %s\n", message);
      }
    }

    console.printf("\n\nBye!\n");
  }

}
