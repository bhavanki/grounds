package xyz.deszaras.grounds.api.method;

import com.google.common.base.Splitter;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.CommandResult;

/**
 * An API command that gets a thing's attribute names.
 */
class GetAttrNamesMethod implements ApiMethod {

  private static final Splitter COMMA_SEP_SPLITTER = Splitter.on(",");

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    String thingId;
    boolean asExtension;
    try {
      thingId = ApiMethodUtils.getRequiredStringParam(request, "thingId");
      asExtension = ApiMethodUtils.getBooleanParam(request, "asExtension", false);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }

    CommandResult getResult =
        ExecMethod.exec(List.of("GET_ATTR_NAMES", thingId), asExtension, ctx);

    if (getResult.isSuccessful()) {
      List<String> names =
          COMMA_SEP_SPLITTER.splitToList(getResult.getResult().toString());
      return new JsonRpcResponse(names, request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INTERNAL_ERROR,
                                                 getResult.getFailureMessageText()),
                                 request.getId());
    }
  }
}
