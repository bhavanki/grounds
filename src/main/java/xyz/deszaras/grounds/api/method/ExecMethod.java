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
    boolean asExtension = ApiMethodUtils.asExtension(request);
    List<String> commandLine;
    try {
      commandLine = ApiMethodUtils.getRequiredStringListParam(request, "commandLine");
    } catch (IllegalArgumentException e) {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INVALID_PARAMETERS,
                                                 e.getMessage()),
                                 request.getId());
    }

    CommandResult result = exec(commandLine, asExtension, ctx);

    if (result.isSuccessful()) {
      return new JsonRpcResponse(result.getResult().toString(),
                                 request.getId());
    } else {
      return new JsonRpcResponse(new ErrorObject(JsonRpcErrorCodes.INTERNAL_ERROR,
                                                 result.getFailureMessageText()),
                                 request.getId());
    }
  }

  /**
   * Executes a command, returning its result. Note that command execution
   * occurs directly, and isn't submitted to the command executor. This is
   * because there should already be a plugin command running, so this command
   * needs to run as part of that, and cannot wait until the plugin command
   * completes.<p>
   *
   * This method is broken out so that other API commands can use it easily.
   *
   * @param  commandLine command line to execute
   * @param  asExtension whether to execute the command as an extension
   * @param  ctx         API method contest
   * @return command result
   */
  public static CommandResult exec(List<String> commandLine, boolean asExtension,
                                   ApiMethodContext ctx) {
    try {
      // FIXME add type safety
      Command commandToExecute = ctx.getCommandExecutor()
          .getCommandFactory().getCommand(ctx.getActor(),
                                          asExtension ? ctx.getExtension() : ctx.getCaller(),
                                          commandLine);
      // TBD: prohibit calling another plugin? What about loops?
      CommandCallable callable =
          new CommandCallable(commandToExecute, ctx.getCommandExecutor());
      return callable.call();
    } catch (CommandFactoryException e) {
      return new CommandResult(e);
    }
  }
}
