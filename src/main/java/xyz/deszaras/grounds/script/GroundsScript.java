package xyz.deszaras.grounds.script;

import java.util.List;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.model.Player;

/**
 * The custom script class for Groovy scripts run by the game. This class
 * defines the API that scripts may use to interact with the game, beyond the
 * Groovy binding.
 */
public abstract class GroundsScript extends groovy.lang.Script {

  private Actor actor;
  private Player player;

  /**
   * Sets the actor for the script.
   *
   * @param actor actor executing the script
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Sets the player for the script.
   *
   * @param player player executing the script
   */
  public void setPlayer(Player player) {
    this.player = player;
  }

  /**
   * Sends a message to the script caller.
   *
   * @param message message to send
   */
  public void sendMessageToCaller(String message) {
    player.sendMessage(message);
  }

  /**
   * Executes a command line. Command execution occurs in the current thread,
   * not through the game's global command executor; in other words, the
   * command runs in the same thread running this script's command. The command
   * is run as the actor / player running this script.
   *
   * @param  commandLine command line to run
   * @return command result
   */
  public CommandResult executeCommand(List<String> commandLine) {
    return new CommandCallable(actor, player, commandLine,
                               CommandExecutor.INSTANCE.getCommandFactory()).call();
  }
}
