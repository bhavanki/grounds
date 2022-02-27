package xyz.deszaras.grounds.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.api.method.ApiMethod;
import xyz.deszaras.grounds.api.method.ApiMethodContext;
import xyz.deszaras.grounds.api.method.ApiMethodFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * A handler for a single API call.
 */
class ApiHandler implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ApiHandler.class);

  private static final int MAX_REQUEST_SIZE = 4096;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @VisibleForTesting
  static final String ERROR_MISSING_JSON_RPC_ID = "Missing JSON RPC ID";
  @VisibleForTesting
  static final String ERROR_MISSING_PLUGIN_CALL_ID = "Missing plugin call ID";
  @VisibleForTesting
  static final String ERROR_UNKNOWN_PLUGIN_CALL_ID_FORMAT = "Unknown plugin call ID %s";
  @VisibleForTesting
  static final String ERROR_UNKNOWN_METHOD_FORMAT = "Unknown method %s";

  private final ByteChannel channel;
  private final PluginCallTracker pluginCallTracker;
  private final ApiMethodFactory apiMethodFactory;
  private final CommandExecutor commandExecutor;

  ApiHandler(ByteChannel channel, PluginCallTracker pluginCallTracker,
             ApiMethodFactory apiMethodFactory, CommandExecutor commandExecutor) {
    this.channel = channel;
    this.pluginCallTracker = pluginCallTracker;
    this.apiMethodFactory = apiMethodFactory;
    this.commandExecutor = commandExecutor;
  }

  @Override
  public void run() {
    ByteBuffer readBuffer = ByteBuffer.allocate(MAX_REQUEST_SIZE);

    try {
      int read = channel.read(readBuffer);
      if (read <= 0) {
        // end of stream
        return;
      }

      JsonRpcResponse response;
      handling: { // https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.15
        JsonRpcRequest request;
        try {
          request = OBJECT_MAPPER.readValue(readBuffer.array(), JsonRpcRequest.class);
        } catch (IOException e) {
          response = new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.PARSE_ERROR,
                                                         null),
                                         null);
          break handling;
        }

        Object jsonRpcId = request.getId();
        if (jsonRpcId == null) {
          response = new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_REQUEST,
                                                         ERROR_MISSING_JSON_RPC_ID),
                                         null);
          break handling;
        }

        Optional<Object> pluginCallId =
            request.getParam(ApiRequestParameters.PLUGIN_CALL_ID);
        if (pluginCallId.isEmpty()) {
          response = new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                         ERROR_MISSING_PLUGIN_CALL_ID),
                                         jsonRpcId);
          break handling;
        }

        Optional<PluginCallTracker.PluginCallInfo> callInfo =
            pluginCallTracker.getInfo(pluginCallId.get().toString());
        if (callInfo.isEmpty()) {
          response = new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                         String.format(ERROR_UNKNOWN_PLUGIN_CALL_ID_FORMAT, pluginCallId.get())),
                                         jsonRpcId);
          break handling;
        }

        Actor actor = callInfo.get().getActor();
        Player caller = callInfo.get().getCaller();
        Extension extension = callInfo.get().getExtension();
        ApiMethodContext ctx = new ApiMethodContext(actor, caller, extension, commandExecutor);

        Optional<ApiMethod> apiMethodOpt =
            apiMethodFactory.getApiMethod(request.getMethod());
        if (apiMethodOpt.isEmpty()) {
          response = new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.METHOD_NOT_FOUND,
                                                         String.format(ERROR_UNKNOWN_METHOD_FORMAT, request.getMethod())),
                                         jsonRpcId);
          break handling;
        }

        response = apiMethodOpt.get().call(request, ctx);
      } // end handling block

      byte[] responseBytes = OBJECT_MAPPER.writeValueAsBytes(response);
      channel.write(ByteBuffer.wrap(responseBytes));
    } catch (IOException e) {
      LOG.error("Failed to process API call", e);
    } finally {
      try {
        channel.close();
      } catch (IOException e) {
        LOG.info("Exception closing channel", e);
      }
    }
  }
}
