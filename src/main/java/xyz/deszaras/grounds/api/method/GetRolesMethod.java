package xyz.deszaras.grounds.api.method;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * An API command that gets a player's roles.
 */
class GetRolesMethod implements ApiMethod {

  private static final Splitter ROLE_SPLITTER = Splitter.on(",");

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    boolean asExtension = ApiMethodUtils.asExtension(request);
    String thingId;
    String playerName;
    try {
      thingId = ApiMethodUtils.getStringParam(request, "thingId", null);
      playerName = ApiMethodUtils.getStringParam(request, "playerName", null);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }
    if (thingId == null && playerName == null) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 "Either thingId or playerName is required"),
                                 request.getId());
    }

    if (playerName != null) {
      Optional<Player> player = Universe.getCurrent().getThingByName(playerName, Player.class);
      if (player.isEmpty()) {
        return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.NOT_FOUND,
                                                   "Player " + playerName + " not found"),
                                   request.getId());
      }
      thingId = player.get().getId().toString();
    }

    CommandResult getResult =
        ExecMethod.exec(List.of("ROLE", "GET", thingId), asExtension, ctx);

    if (getResult.isSuccessful()) {
      String roleCommandResult = getResult.getResult().toString();
      // This relies on RoleCommand#reportRoles() output.
      String rolesString =
          roleCommandResult.substring(roleCommandResult.lastIndexOf(" ") + 1);
      List<String> roles = ROLE_SPLITTER.splitToList(rolesString);
      return new JsonRpcResponse(roles, request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INTERNAL_ERROR,
                                                 getResult.getFailureMessageText()),
                                 request.getId());
    }
  }
}
