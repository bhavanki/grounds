package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import xyz.deszaras.grounds.api.ApiServer;
import xyz.deszaras.grounds.api.PluginCall;
import xyz.deszaras.grounds.api.PluginCallFactory;
import xyz.deszaras.grounds.api.PluginCallFactoryException;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptFactory;
import xyz.deszaras.grounds.script.ScriptFactoryException;
import xyz.deszaras.grounds.server.Server;

/**
 * A factory for commands. This factory is responsible for constructing
 * command objects from tokenized command lines.<p>
 *
 * In order for this factory to build a command, its command class must
 * contain a static {@code newCommand()} method to delegate to.
 */
public class CommandFactory {

  private final List<BiFunction<List<String>, Player, List<String>>> transforms;
  private final Map<String, Class<? extends Command>> commands;
  private final ApiServer apiServer;
  private final Server server;

  /**
   * Creates a new command factory.
   *
   * @param  transforms    functions to transform command lines
   * @param  commands      map of command classes to support, keyed by command name
   * @param  apiServer     API server instance, if running
   * @param  server        server instance, if not in single-user mode
   */
  public CommandFactory(List<BiFunction<List<String>, Player, List<String>>> transforms,
                        Map<String, Class<? extends Command>> commands,
                        ApiServer apiServer, Server server) {
    this.transforms = transforms != null ?
        ImmutableList.copyOf(transforms) : ImmutableList.of();
    this.commands = ImmutableMap.copyOf(commands);
    this.apiServer = apiServer;
    this.server = server;
  }

  /**
   * Gets the class of the command that implements the given command
   * name.
   *
   * @param commandName command name
   * @return supported command, or null if unsupported
   */
  public Class<? extends Command> getCommandClass(String commandName) {
    return commands.get(commandName.toUpperCase());
  }

  /**
   * Gets all of the command names supported by this factory. This does not
   * include the names of scripted commands.
   *
   * @return supported command names
   */
  public Set<String> getCommandNames() {
    return commands.keySet();
  }

  /**
   * Gets an instance of a new command.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param line command line entered in the shell
   * @return command
   * @throws CommandFactoryException if the command cannot be built
   */
  public Command<?> getCommand(Actor actor, Player player, List<String> line)
      throws CommandFactoryException {
    if (line.isEmpty()) {
      return new NoOpCommand(actor, player);
    }

    for (BiFunction<List<String>, Player, List<String>> transform : transforms) {
      line = transform.apply(line, player);
    }

    String commandName = line.get(0);

    if (commandName.startsWith("$")) {
      return newScriptedCommand(actor, player, line);
    }

    Class<? extends Command> commandClass = getCommandClass(commandName);
    if (commandClass == null) {
      throw new CommandFactoryException("Unrecognized command " + commandName);
    }
    List<String> commandArgs = line.subList(1, line.size());

    try {
      Method newCommandMethod;
      if (ServerCommand.class.isAssignableFrom(commandClass)) {
        newCommandMethod =
            commandClass.getMethod("newCommand", Actor.class, Player.class, Server.class, List.class);
        return (Command<?>) newCommandMethod.invoke(null, actor, player, server, commandArgs);
      } else {
        newCommandMethod =
            commandClass.getMethod("newCommand", Actor.class, Player.class, List.class);
        return (Command<?>) newCommandMethod.invoke(null, actor, player, commandArgs);
      }
    } catch (NoSuchMethodException e) {
      throw new CommandFactoryException("Command class " + commandClass.getName() +
                                        " lacks a static newCommand method!");
    } catch (IllegalAccessException e) {
      throw new CommandFactoryException("Failed to create new command", e);
    } catch (InvocationTargetException e) {
      throw new CommandFactoryException("Failed to create new command", e.getCause());
    }
  }

  /**
   * Gets a scripted command. This method hunts for the script attribute
   * defining the command among all extensions in the universe.
   *
   * @param actor actor submitting the command
   * @param player player currently assumed by the actor
   * @param line command line entered in the shell
   * @return scripted command
   * @throws CommandFactoryException if the command cannot be built
   */
  private Command newScriptedCommand(Actor actor, Player player, List<String> line)
      throws CommandFactoryException {
    String commandName = line.get(0);
    List<String> scriptArguments = line.subList(1, line.size());

    // In progress replacement of scripts with plugins.
    try {
      Optional<PluginCall> pluginCall = findPluginCall(commandName);
      if (pluginCall.isPresent()) {
        return PluginCallCommand.newCommand(actor, player, pluginCall.get(),
                                            scriptArguments);
      }
    } catch (PluginCallFactoryException e) {
      throw new CommandFactoryException("Failed to build plugin call for command " + commandName, e);
    }

    Script script;
    try {
      script = findScript(commandName).orElseThrow(() ->
          new CommandFactoryException("Failed to locate script for command " + commandName));
    } catch (ScriptFactoryException e) {
      throw new CommandFactoryException("Failed to build script for command " + commandName, e);
    }

    return ScriptedCommand.newCommand(actor, player, script, scriptArguments);
  }

  public PluginCallCommand newPluginCallCommand(Actor actor, Player player, Attr pluginCallAttr,
                                                Extension extension, List<String> pluginCallArguments)
      throws CommandFactoryException {
    if (apiServer == null) {
      throw new CommandFactoryException("API server is not available");
    }
    try {
      PluginCall pluginCall = new PluginCallFactory()
          .newPluginCall(pluginCallAttr, extension, apiServer.getPluginCallTracker());
      return PluginCallCommand.newCommand(actor, player, pluginCall, pluginCallArguments);
    } catch (PluginCallFactoryException e) {
      throw new CommandFactoryException("Failed to create plugin call for command", e);
    }
  }

  /**
   * Finds a plugin call for the given command name among the extensions in the
   * universe.
   *
   * @param  commandName command name
   * @return             plugin call implementing the command
   * @throws PluginCallFactoryException if the plugin call could not be constructed
   */
  public Optional<PluginCall> findPluginCall(String commandName)
      throws PluginCallFactoryException {
    if (apiServer == null) {
      // FUTURE: Once script support is gone, return an error here instead
      // because no plugin calls can work
      return Optional.empty();
    }

    // Find the attribute defining the command. It must be attached
    // to an extension and have an attribute list as a value.
    Optional<Attr> pluginCallAttr = Optional.empty();
    Extension pluginCallExtension = null;
    // this is inefficient :(
    for (Extension extension : Universe.getCurrent().getThings(Extension.class)) {
      Optional<Attr> extPluginCallAttr = extension.getAttr(commandName, Attr.Type.ATTRLIST);
      if (extPluginCallAttr.isPresent() &&
          // temporary: look for "pluginMethod" to distinguish this from a script
          extPluginCallAttr.get().getAttrInAttrListValue("pluginMethod").isPresent()) {
        pluginCallAttr = extPluginCallAttr;
        pluginCallExtension = extension;
        break;
      }
    }

    if (pluginCallAttr.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new PluginCallFactory()
                       .newPluginCall(pluginCallAttr.get(), pluginCallExtension,
                                      apiServer.getPluginCallTracker()));
  }

  /**
   * Finds a script for the given command name among the extensions in the
   * universe.
   *
   * @param  commandName            command name
   * @return                        script implementing the command
   * @throws ScriptFactoryException if the script could not be constructed
   */
  public Optional<Script> findScript(String commandName) throws ScriptFactoryException {
    // Find the attribute defining the command. It must be attached
    // to an extension and have an attribute list as a value.
    Optional<Attr> scriptAttr = Optional.empty();
    Extension scriptExtension = null;
    // this is inefficient :(
    for (Extension extension : Universe.getCurrent().getThings(Extension.class)) {
      scriptAttr = extension.getAttr(commandName, Attr.Type.ATTRLIST);
      if (scriptAttr.isPresent()) {
        scriptExtension = extension;
        break;
      }
    }

    if (scriptAttr.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ScriptFactory().newScript(scriptAttr.get(), scriptExtension));
  }
}
