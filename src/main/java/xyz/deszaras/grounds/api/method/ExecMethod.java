package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;

/**
 * An API method that executes a Grounds command.
 */
class ExecMethod implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    List<String> commandLine;
    boolean asExtension;
    try {
      commandLine = ApiMethodUtils.getRequiredStringListParam(request, "commandLine");
      asExtension = ApiMethodUtils.getBooleanParam(request, "asExtension", false);
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }


    // Note that command execution occurs directly, and isn't submitted to the
    // command executor. This is because there should already be a plugin
    // command running, so this command needs to run as part of that, and cannot
    // wait until the plugin command completes.
    CommandResult result;
    try {
      Command commandToExecute = ctx.getCommandExecutor()
          .getCommandFactory().getCommand(ctx.getActor(),
                                          asExtension ? ctx.getExtension() : ctx.getCaller(),
                                          commandLine);
      CommandCallable callable =
          new CommandCallable(commandToExecute, ctx.getCommandExecutor());
      result = callable.call();
    } catch (CommandFactoryException e) {
      result = new CommandResult(e);
    }

    if (result.isSuccessful()) {
      return new JsonRpcResponse(result.getResult().toString(),
                                 request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(1234, result.getFailureMessageText()),
                                 request.getId());
    }
  }
}
