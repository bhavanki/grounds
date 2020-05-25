package xyz.deszaras.grounds.script;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * The custom script class for Groovy scripts run by the game. This class
 * defines the API that scripts may use to interact with the game, beyond the
 * Groovy binding.
 */
public abstract class GroundsScript extends groovy.lang.Script {

  private Actor actor;
  private Player caller;
  private Player runner;
  private Player owner;

  /**
   * Sets the actor for the script.
   *
   * @param actor actor executing the script
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Sets the player (and caller) for the script.
   *
   * @param player player executing the script
   */
  public void setPlayer(Player player) {
    this.caller = player;
    this.runner = player;
  }

  /**
   * Sets the owner for the script.
   *
   * @param owner owner of the script
   */
  public void setOwner(Player owner) {
    this.owner = owner;
  }

  /**
   * Continues execution of this script as the script's owner. This allows for
   * scripts to execute commands that the original caller cannot.
   */
  public void runAsOwner() {
    this.runner = owner;
  }

  /**
   * Gets the name of the caller of this script.
   *
   * @return caller name
   */
  public String getCallerName() {
    return caller.getName();
  }

  /**
   * Gets one of a thing's attributes by name. An attribute is immutable.
   *
   * @param thingId ID of thing with attribute
   * @param name attribute name
   * @return attribute
   * @throws CommandException if the player may not access the thing
   */
  public Optional<Attr> getAttr(String thingId, String name) throws CommandException {
    Optional<Thing> thing = runner.getUniverse().getThing(UUID.fromString(thingId));
    if (thing.isEmpty()) {
      return Optional.empty();
    }
    // so maybe this should be a command
    if (!thing.get().passes(Category.READ, runner)) {
      throw new CommandException("You may not read attributes for that");
    }
    return thing.get().getAttr(name);
  }

  /**
   * Sends a message to the script caller.
   *
   * @param message message to send
   */
  public void sendMessageToCaller(String message) {
    caller.sendMessage(new Message(runner, Message.Style.SCRIPT, message));
  }

  /**
   * Sends a message to a player. The message is sent from the script's player.
   *
   * @param playerName name of player
   * @param message message to send
   */
  public void sendMessageTo(String playerName, String message) {
    Optional<Player> targetPlayer =
        runner.getUniverse().getThingByName(playerName, Player.class);
    if (targetPlayer.isPresent()) {
      targetPlayer.get().sendMessage(new Message(runner, Message.Style.SCRIPT, message));
    }
  }

  /**
   * Executes a command line. Command execution occurs in the current thread,
   * not through the game's global command executor; in other words, the
   * command runs in the same thread running this script's command. The command
   * is run as the player running this script.
   *
   * @param  commandLine command line to run
   * @return command result
   */
  public CommandResult exec(List<String> commandLine) {
    return new CommandCallable(actor, runner, commandLine,
                               CommandExecutor.INSTANCE.getCommandFactory()).call();
  }

  /**
   * Creates an exception to throw when the script fails.
   *
   * @param message failure message
   */
  public CommandException failure(String message) {
    return new CommandException(message);
  }
}
