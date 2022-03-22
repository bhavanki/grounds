package xyz.deszaras.grounds.api.method;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;

/**
 * An API method that gets the name of the plugin caller. So, this is not really
 * the name of the caller to this method itself. The expectation is that this
 * method is called from a plugin which is serving its own plugin call, and that
 * plugin wants to know who called it.
 */
class GetCallerNameMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    return new JsonRpcResponse(ctx.getCaller().getName(), request.getId());
  }
}
