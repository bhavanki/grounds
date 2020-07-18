package xyz.deszaras.grounds.script;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * The custom script class for Groovy scripts run by the game. This class
 * defines the API that scripts may use to interact with the game, beyond the
 * Groovy binding.
 */
public abstract class GroundsScript extends groovy.lang.Script {

  private static final Splitter COMMA_SEP_SPLITTER = Splitter.on(",");

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
   * Checks if a thing has an attribute.
   *
   * @param  thingId                 ID of thing
   * @param  name                    attribute name
   * @return                         true if thing has attribute
   */
  public boolean hasAttr(String thingId, String name)
      throws CommandFactoryException {
    CommandResult getResult = exec(List.of("GET_ATTR", thingId, name));
    try {
      throwFailureExceptionIfPresent(getResult);
    } catch (CommandException e) {
      return false;
    }
    return true;
  }

  /**
   * Gets a thing's attributes by name. An attribute is immutable.
   *
   * @param  thingId                 ID of thing with attribute
   * @param  name                    attribute name
   * @param  notFoundMessage         exception message when thing is not found
   * @return attribute
   */
  public Attr getAttr(String thingId, String name, String notFoundMessage)
      throws CommandException, CommandFactoryException {
    CommandResult getResult = exec(List.of("GET_ATTR", thingId, name));
    try {
      throwFailureExceptionIfPresent(getResult);
    } catch (CommandException e) {
      throw new CommandException(notFoundMessage);
    }
    return Attr.fromAttrSpec(getResult.getResult().toString());
  }

  /**
   * Gets the names of a thing's attributes.
   *
   * @param  thingId                 ID of thing with attributes
   * @param  notFoundMessage         exception message when thing is not found
   * @return                         attribute names
   */
  public List<String> getAttrNames(String thingId, String notFoundMessage)
      throws CommandException, CommandFactoryException {
    CommandResult getResult = exec(List.of("GET_ATTR_NAMES", thingId));
    try {
      throwFailureExceptionIfPresent(getResult);
    } catch (CommandException e) {
      throw new CommandException(notFoundMessage);
    }
    return COMMA_SEP_SPLITTER.splitToList(getResult.getResult().toString());
  }

  /**
   * Creates a new attribute with a string value.
   *
   * @param  name  attribute name
   * @param  value attribute value
   * @return       new attribute
   */
  public Attr newAttr(String name, String value) {
    return new Attr(name, value);
  }

  /**
   * Sets a thing's attribute with a string value.
   *
   * @param  thingId ID of thing
   * @param  name    attribute name
   * @param  value   attribute value
   */
  public void setAttr(String thingId, String name, String value)
      throws CommandException, CommandFactoryException {
    Attr newAttr = new Attr(name, value);
    setAttr(thingId, newAttr);
  }

  /**
   * Set's a thing's attribute with an attribute list value.
   *
   * @param  thingId ID of thing
   * @param  name    attribute name
   * @param  value   attribute value
   * @return       new attribute
   */
  public void setAttr(String thingId, String name, List<Attr> value)
      throws CommandException, CommandFactoryException {
    Attr newAttr = new Attr(name, value);
    setAttr(thingId, newAttr);
  }

  private void setAttr(String thingId, Attr newAttr)
      throws CommandException, CommandFactoryException {
    CommandResult setResult = exec(List.of("SET_ATTR", thingId, newAttr.toAttrSpec()));
    throwFailureExceptionIfPresent(setResult);
  }

  /**
   * Removes a thing's attribute.
   *
   * @param  thingId                 ID of thing
   * @param  name                    attribute name
   */
  public void removeAttr(String thingId, String name)
      throws CommandException, CommandFactoryException {
    CommandResult removeResult = exec(List.of("REMOVE_ATTR", thingId, name));
    throwFailureExceptionIfPresent(removeResult);
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
        Universe.getCurrent().getThingByName(playerName, Player.class);
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
                               CommandExecutor.getInstance().getCommandFactory()).call();
  }

  /**
   * Creates an exception to throw when the script fails.
   *
   * @param message failure message
   */
  public CommandException failure(String message) {
    return new CommandException(message);
  }

  /**
   * Throws any exception recorded in a command result, or none if the result
   * indicates success.
   *
   * @param  result                  command result
   * @throws CommandException        if the result indicates failure due to
   *                                 command execution
   * @throws CommandFactoryException if the result indicates failure due to
   *                                 building the command
   */
  private void throwFailureExceptionIfPresent(CommandResult result)
      throws CommandException, CommandFactoryException {
    if (result.isSuccessful()) {
      return;
    }

    Optional<CommandException> ce = result.getCommandException();
    if (ce.isPresent()) {
      throw ce.get();
    }
    Optional<CommandException> cfe = result.getCommandFactoryException();
    throw cfe.get();
  }
}
