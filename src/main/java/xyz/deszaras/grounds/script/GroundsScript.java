package xyz.deszaras.grounds.script;

import com.google.common.base.Splitter;

import groovy.json.JsonSlurper;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.RecordOutput;
import xyz.deszaras.grounds.util.TabularOutput;

/**
 * The custom script class for Groovy scripts run by the game. This class
 * defines the API that scripts may use to interact with the game, beyond the
 * Groovy binding.
 */
public abstract class GroundsScript extends groovy.lang.Script {

  private static final Logger LOG = LoggerFactory.getLogger(GroundsScript.class);

  private static final Splitter COMMA_SEP_SPLITTER = Splitter.on(",");

  private Actor actor;
  private Player caller;
  private Player runner;
  private Extension extension;

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
   * Sets the extension for the script.
   *
   * @param extension extension of the script
   */
  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  /**
   * Continues execution of this script as the script's extension. This allows
   * for scripts to execute commands that the original caller cannot.
   */
  public void runAsExtension() {
    this.runner = extension;
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
   * Gets the timezone of the caller of this script.
   *
   * @return caller timezone
   */
  public ZoneId getCallerTimezone() {
    Optional<Actor> actor = caller.getCurrentActor();
    if (actor.isPresent()) {
      return actor.get().getTimezone();
    } else {
      return ZoneOffset.UTC;
    }
  }

  /**
   * Parses the given JSON string.
   *
   * @param  s JSON string
   * @return   parsed JSON
   */
  public Object parseJson(String s) {
    return new JsonSlurper().parseText(s);
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
   * Gets an attribute from a thing's attribute list value. Here, "name" is the
   * name of the attribute whose value is a list of attributes, and "subname"
   * is the name of the attribute to get in that list.
   *
   * @param  thingId                 ID of thing
   * @param  name                    name of top-level attribute
   * @param  subname                 name of attribute in list
   * @param  notFoundMessage         exception message when thing or attribute is not found
   * @return                         attribute from list
   */
  public Attr getAttrInAttrList(String thingId, String name, String subname, String notFoundMessage)
      throws CommandException, CommandFactoryException {
    Attr attr = getAttr(thingId, name, notFoundMessage);
    for (Attr subAttr : attr.getAttrListValue()) {
      if (subAttr.getName().equals(subname)) {
        return subAttr;
      }
    }
    throw new CommandException(notFoundMessage);
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
   * Creates a new attribute with a timestamp value.
   *
   * @param  name  attribute name
   * @param  value attribute value
   * @return       new attribute
   */
  public Attr newAttr(String name, Instant value) {
    return new Attr(name, value);
  }

  /**
   * Creates a new attribute with a thing value.
   *
   * @param  name  attribute name
   * @param  value attribute value
   * @return       new attribute
   */
  public Attr newAttr(String name, Thing value) {
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
   * Sets a thing's attribute with an attribute list value.
   *
   * @param  thingId ID of thing
   * @param  name    attribute name
   * @param  value   attribute value
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
   * Sets an attribute in a thing's attribute list value. Here, "name" is the
   * name of the attribute whose value is a list of attributes, and "subname"
   * is the name of the attribute to set in that list. If subvalue is null, the
   * attribute is removed from the list.
   *
   * @param  thingId                 ID of thing
   * @param  name                    name of top-level attribute
   * @param  subname                 name of attribute in list
   * @param  subvalue                value of attribute in list (null to remove)
   */
  public void setAttrInAttrListValue(String thingId, String name, String subname, String subvalue)
      throws CommandException, CommandFactoryException {
    Attr attr = getAttr(thingId, name, "Thing " + name + " not found");
    List<Attr> value = new ArrayList<>();
    for (Attr a : attr.getAttrListValue()) {
      if (!a.getName().equals(subname)) {
        value.add(a);
      }
    }
    if (subvalue != null) {
      Attr newSubAttr = new Attr(subname, subvalue);
      value.add(newSubAttr);
    }
    setAttr(thingId, name, value);
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
        AccessController.doPrivileged(new PrivilegedAction<Optional<Player>>() {
          @Override
          public Optional<Player> run() {
            return Universe.getCurrent().getThingByName(playerName, Player.class);
          }
        });
    if (targetPlayer.isPresent()) {
      targetPlayer.get().sendMessage(new Message(runner, Message.Style.SCRIPT, message));
    }
  }

  /**
   * Gets an empty tabular output for constructing formatted table data.
   *
   * @return new tabular output
   */
  public TabularOutput newTabularOutput() {
    return new TabularOutput();
  }

  /**
   * Gets an empty record output for constructing formatted key/value data.
   *
   * @return new recor output
   */
  public RecordOutput newRecordOutput() {
    return new RecordOutput();
  }
  /**
   * Executes a command line. Command execution occurs in the current thread,
   * not directly through the game's global command executor; in other words, the
   * command runs in the same thread running this script's command. The command
   * is run as the player running this script.
   *
   * @param  commandLine command line to run
   * @return command result
   */
  public CommandResult exec(List<String> commandLine) {
    return AccessController.doPrivileged(new PrivilegedAction<CommandResult>() {
      @Override
      public CommandResult run() {
        try {
          Command commandToExecute =
              CommandExecutor.getInstance().getCommandFactory().getCommand(actor, runner, commandLine);
          CommandCallable callable =
              new CommandCallable(commandToExecute, CommandExecutor.getInstance());
          return callable.call();
        } catch (CommandFactoryException e) {
          return new CommandResult(e);
        }
      }
    });
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

  /**
   * Emits a DEBUG-level log message.
   *
   * @param message log message
   */
  public void logDebug(String message) {
    LOG.debug(message);
  }

  /**
   * Emits an ERROR-level log message.
   *
   * @param message log message
   */
  public void logError(String message) {
    LOG.error(message);
  }
}
