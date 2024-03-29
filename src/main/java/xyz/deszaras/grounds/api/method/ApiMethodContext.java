package xyz.deszaras.grounds.api.method;

import java.util.Objects;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * Context for an individual API call. An {@link ApiMethod} should be able to
 * execute based on the data in this context.
 */
public class ApiMethodContext {

  private final Actor actor;
  private final Player caller;
  private final Extension extension;
  private final CommandExecutor commandExecutor;

  public ApiMethodContext(Actor actor, Player caller, Extension extension,
                          CommandExecutor commandExecutor) {
    this.actor = Objects.requireNonNull(actor);
    this.caller = Objects.requireNonNull(caller);
    this.extension = Objects.requireNonNull(extension);
    this.commandExecutor = Objects.requireNonNull(commandExecutor);
  }

  public Actor getActor() {
    return actor;
  }

  public Player getCaller() {
    return caller;
  }

  public Extension getExtension() {
    return extension;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
}
