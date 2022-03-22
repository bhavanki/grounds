package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.CommandResult;

/**
 * An API command that removes a thing's attribute by name.
 */
class RemoveAttrMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    String thingId;
    String name;
    boolean asExtension;
    try {
      thingId = ApiMethodUtils.getRequiredStringParam(request, "thingId");
      name = ApiMethodUtils.getRequiredStringParam(request, "name");
      asExtension = ApiMethodUtils.getBooleanParam(request, "asExtension", false);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }

    CommandResult removeResult =
        ExecMethod.exec(List.of("REMOVE_ATTR", thingId, name), asExtension, ctx);

    if (removeResult.isSuccessful()) {
      return new JsonRpcResponse("", request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INTERNAL_ERROR,
                                                 removeResult.getFailureMessageText()),
                                 request.getId());
    }
  }
}
