package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;
import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandCallable;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.CommandResult;

/**
 * An API command that executes a Grounds command.
 */
class ExecCommand implements ApiMethod {

  @Override
  public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
    List<String> commandLine;
    try {
      commandLine = request.getStringListParam("commandLine")
          .orElseThrow(() -> new IllegalArgumentException("Param commandLine missing"));
    } catch (IllegalArgumentException | ClassCastException e) {
      return new JsonRpcResponse(new ErrorObject(12345, e.getMessage()),
                                 request.getId());
    }

    boolean asExtension = request.getBooleanParam("asExtension").orElse(Boolean.FALSE);

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
