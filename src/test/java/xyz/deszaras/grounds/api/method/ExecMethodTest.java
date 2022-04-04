package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.api.JsonRpcResponse;

public class ExecMethodTest extends ApiMethodTest {

  private ExecMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new ExecMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, true);
    List<String> commandLine = List.of("SAY", "hello");
    request = new JsonRpcRequest("exec", Map.<String, Object>of(
        "commandLine", commandLine
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("true", result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallAsExtension() throws Exception {
    mockSuccessfulCommand(extension, true);
    List<String> commandLine = List.of("SAY", "hello");
    request = new JsonRpcRequest("exec", Map.<String, Object>of(
        "commandLine", commandLine,
        "_as_extension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("true", result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, extension, commandLine);
  }

  @Test
  public void testCallMissingCommandLine() throws Exception {
    request = new JsonRpcRequest("exec", Map.<String, Object>of());

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallExecFactoryFailure() throws Exception {
    mockFactoryFailedCommand(caller, "oopsie");
    List<String> commandLine = List.of("SAY", "hello");
    request = new JsonRpcRequest("exec", Map.<String, Object>of(
        "commandLine", commandLine
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }

  @Test
  public void testCallExecFailure() throws Exception {
    mockFailedCommand(caller, "oopsie");
    List<String> commandLine = List.of("SAY", "hello");
    request = new JsonRpcRequest("exec", Map.<String, Object>of(
        "commandLine", commandLine
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
