package xyz.deszaras.grounds.api.method;

import java.time.ZoneOffset;
import java.util.Optional;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.command.Actor;

/**
 * An API method that gets the timezone of the plugin caller. See
 * {@link GetCallerNameCommand} for more on who the caller is.
 */
class GetCallerTimezoneMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    Optional<Actor> actor = ctx.getCaller().getCurrentActor();
    String tzString;
    if (actor.isPresent()) {
      tzString = actor.get().getTimezone().toString();
    } else {
      tzString = ZoneOffset.UTC.toString();
    }
    return new JsonRpcResponse(tzString, request.getId());
  }
}
