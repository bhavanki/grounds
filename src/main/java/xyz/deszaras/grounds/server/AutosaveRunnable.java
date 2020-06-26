package xyz.deszaras.grounds.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * This runnable saves the current universe, if possible. Instances of this
 * class live on the server's administrative queue.
 */
public class AutosaveRunnable implements Runnable {

  private static final Logger LOG =
      LoggerFactory.getLogger(AutosaveRunnable.class);

  private final CommandExecutor commandExecutor;

  /**
   * Creates a new callable.
   */
  public AutosaveRunnable(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  private static class AutosaveCommand extends Command<Boolean> {

    private AutosaveCommand() {
      super(Actor.ROOT, Player.GOD);
    }

    @Override
    public Boolean execute() {
      try {
        Universe.saveCurrent();
        LOG.info("Autosaved current universe");
        return true;
      } catch (IOException e) {
        LOG.error("Failed to autosave current universe", e);
      }
      return false;
    }
  }

  @Override
  public void run() {
    commandExecutor.submit(new AutosaveCommand());
  }
}
