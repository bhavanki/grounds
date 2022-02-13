package xyz.deszaras.grounds.api.method;

import java.util.Objects;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;

/**
 * Context for an individual API call. An {@link ApiMethod} should be able to
 * execute based on the data in this context.
 */
public class ApiMethodContext {

  private final Actor actor;
  private final Player runner;
  private final CommandExecutor commandExecutor;

  public ApiMethodContext(Actor actor, Player runner, CommandExecutor commandExecutor) {
    this.actor = Objects.requireNonNull(actor);
    this.runner = Objects.requireNonNull(runner);
    this.commandExecutor = Objects.requireNonNull(commandExecutor);
  }

  public Actor getActor() {
    return actor;
  }

  public Player getRunner() {
    return runner;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
}
