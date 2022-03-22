package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.model.Attr;

/**
 * An API command that gets a thing's attributes by name.
 */
class SetAttrMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    String thingId;
    String name;
    String value;
    String type;
    boolean asExtension;
    try {
      thingId = ApiMethodUtils.getRequiredStringParam(request, "thingId");
      name = ApiMethodUtils.getRequiredStringParam(request, "name");
      value = ApiMethodUtils.getRequiredStringParam(request, "value");
      type = ApiMethodUtils.getRequiredStringParam(request, "type");
      asExtension = ApiMethodUtils.getBooleanParam(request, "asExtension", false);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }

    Attr a;
    try {
      a = new Attr(name, value, Attr.Type.valueOf(type));
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 "Invalid type " + type),
                                 request.getId());
    }

    CommandResult setResult =
        ExecMethod.exec(List.of("SET_ATTR", thingId, a.toAttrSpec()), asExtension, ctx);

    if (setResult.isSuccessful()) {
      return new JsonRpcResponse("", request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INTERNAL_ERROR,
                                                 setResult.getFailureMessageText()),
                                 request.getId());
    }
  }
}
