package xyz.deszaras.grounds.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * A callable for executing a {@link PluginCall}.
 */
public class PluginCallable implements Callable<String> {

  private static final Logger LOG = LoggerFactory.getLogger(PluginCallable.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Actor actor;
  private final Player player;
  private final PluginCall pluginCall;
  private final List<String> arguments;

  /**
   * Creates a new callable.
   *
   * @param  actor      actor executing the plugin call
   * @param  player     player executing the plugin call
   * @param  pluginCall plugin call to execute
   * @param  arguments  arguments to pass to the plugin call
   * @throws NullPointerException if any argument is null
   */
  public PluginCallable(Actor actor, Player player, PluginCall pluginCall,
                        List<String> arguments) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
    this.pluginCall = Objects.requireNonNull(pluginCall);
    this.arguments = ImmutableList.copyOf(arguments);
  }

  /**
   * Executes the plugin call.<p>
   *
   * A JSON-RPC request is built containing the call arguments and metadata such
   * as the plugin call ID (needed if the plugin wants to call the API). The
   * request is sent to the plugin, and its response is handled. The result in
   * the plugin call response is returned by this method, replacing it with null
   * if it's empty.<p>
   *
   * Tracking of the plugin call is started just before the call occurs, and is
   * removed regardless of the call outcome.
   *
   * @return result of plugin call
   */
  @Override
  public String call() throws CommandException {

    // Check that the caller has a permitted role.
    if (!(player.equals(Player.GOD))) {
      Set<Role> playerRoles = new HashSet<>(Universe.getCurrent().getRoles(player));
      playerRoles.retainAll(pluginCall.getCallerRoles());
      if (playerRoles.isEmpty()) {
        throw new PermissionException("Permission denied");
      }
    }

    // Construct request parameters.
    String pluginCallId = UUID.randomUUID().toString();
    Map<String, Object> requestParameters = new HashMap<>();
    requestParameters.put(PluginCallRequestParameters.PLUGIN_CALL_ID, pluginCallId);
    requestParameters.put(PluginCallRequestParameters.EXTENSION_ID,
                          pluginCall.getExtension().getId().toString());
    requestParameters.put(PluginCallRequestParameters.PLUGIN_CALL_ARGUMENTS, arguments);

    // Establish tracking for the call.
    PluginCallTracker.PluginCallInfo info =
        new PluginCallTracker.PluginCallInfo(actor, player, pluginCall.getExtension());
    // TBD: Include extension in tracked info?
    pluginCall.getPluginCallTracker().track(pluginCallId, info);

    // Send the request and receive a response.
    JsonRpcRequest request = new JsonRpcRequest(pluginCall.getMethod(),
                                                requestParameters);
    if (LOG.isDebugEnabled()) {
      try {
        LOG.debug("JSON RPC request: {}",
                 OBJECT_MAPPER.writeValueAsString(request));
      } catch (JsonProcessingException e) {
        LOG.debug("JSON RPC request could not be serialized for logging");
      }
    }
    JsonRpcResponse response;
    int pluginExitCode;
    try {
      Process pluginProcess = new ProcessBuilder(pluginCall.getPluginPath())
          .start();

      OutputStream stdin = pluginProcess.getOutputStream();
      stdin.write(OBJECT_MAPPER.writeValueAsBytes(request));
      stdin.close();

      InputStream stdout = pluginProcess.getInputStream();
      response = OBJECT_MAPPER.readValue(stdout, JsonRpcResponse.class);

      pluginExitCode = pluginProcess.waitFor();
      // TBD: timeout
    } catch (InterruptedException e) {
      LOG.error("Interrupted waiting for plugin call {} in extension {} for {}",
                pluginCall.toString(), pluginCall.getExtension().getId(), player.getName(), e);
      throw new CommandException("Interrupted waiting for plugin call " + pluginCall.toString() +
                                 " in extension " +  pluginCall.getExtension().getId().toString(),
                                 e);
    } catch (IOException e) {
      LOG.error("Failed to execute plugin call {} in extension {} for {}",
                pluginCall.toString(), pluginCall.getExtension().getId(), player.getName(), e);
      throw new CommandException("Failed to execute plugin call " + pluginCall.toString() +
                                 " in extension " + pluginCall.getExtension().getId().toString(),
                                 e);
    } finally {
      pluginCall.getPluginCallTracker().untrack(pluginCallId);
    }

    // Does this actually matter?
    if (pluginExitCode != 0) {
      LOG.warn("Plugin call {} in extension {} for {} returned exit code {}",
               pluginCall.toString(), pluginCall.getExtension().getId(), player.getName(),
               pluginExitCode);
    }

    if (response.isSuccessful()) {
      Object resultObj = response.getResult();
      if (resultObj == null) {
        return null;
      }
      if (!(resultObj instanceof String)) {
        LOG.error("Plugin call {} in extension {} for {} returned result of type {}",
                  pluginCall.toString(), pluginCall.getExtension().getId(), player.getName(),
                  resultObj.getClass().toString());
        throw new CommandException("Plugin call " + pluginCall.toString() +
                                   " in extension " + pluginCall.getExtension().getId() +
                                   " returned result of type " + resultObj.getClass().toString());
      }

      String result = (String) resultObj;
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
    LOG.error("Plugin call {} in extension {} for {} returned an error: [{}] {}",
              pluginCall.toString(), pluginCall.getExtension().getId(), player.getName(),
              response.getError().getCode(),
              response.getError().getMessage());
    throw new CommandException("Plugin error: " +
                               response.getError().getMessage());
  }
}
