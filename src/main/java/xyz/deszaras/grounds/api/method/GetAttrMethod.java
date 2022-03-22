package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.model.Attr;

/**
 * An API command that gets a thing's attribute by name.
 */
class GetAttrMethod implements ApiMethod {

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

    CommandResult getResult =
        ExecMethod.exec(List.of("GET_ATTR", thingId, name), asExtension, ctx);

    if (getResult.isSuccessful()) {
      Attr a = Attr.fromAttrSpec(getResult.getResult().toString());
      return new JsonRpcResponse(a, request.getId());
    } else {
      String failureMessageText = getResult.getFailureMessageText();
      int errorCode;
      if (failureMessageText.contains("There is no attribute")) {
        errorCode = JsonRpcErrorCodes.NOT_FOUND;
      } else {
        errorCode = JsonRpcErrorCodes.INTERNAL_ERROR;
      }
      return new JsonRpcResponse(new ErrorObject(errorCode, failureMessageText),
                                 request.getId());
    }
  }
}
