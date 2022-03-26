package xyz.deszaras.grounds.api.method;

import java.util.HashMap;
import java.util.Map;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;

/**
 * An API method that sends a message to the plugin caller. See
 * {@link GetCallerNameCommand} for more on who the caller is.
 */
class SendMessageToCallerMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    Map<String, Object> params = new HashMap<>(request.getParams());
    params.put("playerName", ctx.getCaller().getName());
    JsonRpcRequest delegateRequest =
        new JsonRpcRequest(request.getMethod(), params, request.getId());
    return new SendMessageMethod().call(delegateRequest, ctx);
  }

}
