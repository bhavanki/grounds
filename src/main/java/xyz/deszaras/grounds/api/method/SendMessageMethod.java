package xyz.deszaras.grounds.api.method;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.RecordOutput;
import xyz.deszaras.grounds.util.TabularOutput;

/**
 * An API method that sends a message to a player.
 */
class SendMessageMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    boolean asExtension = ApiMethodUtils.asExtension(request);
    String playerName;
    String message;
    Map<String, List<String>> recordMap;
    Map<String, List<List<String>>> tableMap;
    String header;
    try {
      playerName = ApiMethodUtils.getRequiredStringParam(request, "playerName");
      message = ApiMethodUtils.getStringParam(request, "message", null);
      recordMap = ApiMethodUtils.getParam(request, "record", Map.class, null);
      tableMap = ApiMethodUtils.getParam(request, "table", Map.class, null);
      header = ApiMethodUtils.getStringParam(request, "header", null);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }
    if (message == null && recordMap == null && tableMap == null) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 "Either message, record, or table is required"),
                                 request.getId());
    }

    Optional<Player> targetPlayer =
        Universe.getCurrent().getThingByName(playerName, Player.class);
    if (targetPlayer.isPresent()) {
      Player sender = asExtension ? ctx.getExtension() : ctx.getCaller();
      if (tableMap != null) {
        try {
          message = TabularOutput.from(tableMap).toString();
        } catch (IllegalArgumentException | ClassCastException e) {
          return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                     "Invalid table: " + e.getMessage()),
                                     request.getId());
        }

      } else if (recordMap != null) {
        try {
          message = RecordOutput.from(recordMap).toString();
        } catch (IllegalArgumentException | ClassCastException e) {
          return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                     "Invalid record: " + e.getMessage()),
                                     request.getId());
        }
      }
      if (header != null) {
        message = header + "\n" + message;
      }
      targetPlayer.get().sendMessage(new Message(sender, Message.Style.SCRIPT, message));
    }

    return new JsonRpcResponse("", request.getId());
  }
}
