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

public class RemoveAttrMethodTest extends ApiMethodTest {

  private RemoveAttrMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new RemoveAttrMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, true);
    List<String> commandLine = List.of("REMOVE_ATTR", "thingId", "attrName");
    request = new JsonRpcRequest("removeAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallAsExtension() throws Exception {
    mockSuccessfulCommand(extension, true);
    List<String> commandLine = List.of("REMOVE_ATTR", "thingId", "attrName");
    request = new JsonRpcRequest("removeAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName",
        "_as_extension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, extension, commandLine);
  }

  @Test
  public void testCallMissingThingId() throws Exception {
    request = new JsonRpcRequest("removeAttr", Map.<String, Object>of(
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallMissingName() throws Exception {
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId"
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallExecFailure() throws Exception {
    mockFailedCommand(caller, "oopsie");
    request = new JsonRpcRequest("removeAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
