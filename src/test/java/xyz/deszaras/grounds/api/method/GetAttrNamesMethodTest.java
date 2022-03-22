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

public class GetAttrNamesMethodTest extends ApiMethodTest {

  private GetAttrNamesMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new GetAttrNamesMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, "attr1,attr2,attr3");
    List<String> commandLine = List.of("GET_ATTR_NAMES", "thingId");
    request = new JsonRpcRequest("getAttrNames", Map.<String, Object>of(
        "thingId", "thingId"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(List.of("attr1", "attr2", "attr3"), result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallAsExtension() throws Exception {
    mockSuccessfulCommand(extension, "attr1,attr2,attr3");
    List<String> commandLine = List.of("GET_ATTR_NAMES", "thingId");
    request = new JsonRpcRequest("getAttrNames", Map.<String, Object>of(
        "thingId", "thingId",
        "asExtension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(List.of("attr1", "attr2", "attr3"), result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, extension, commandLine);
  }

  @Test
  public void testCallMissingThingId() throws Exception {
    request = new JsonRpcRequest("getAttrNames", Map.<String, Object>of());

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallExecFailure() throws Exception {
    mockFailedCommand(caller, "oopsie");
    request = new JsonRpcRequest("getAttrNames", Map.<String, Object>of(
        "thingId", "thingId"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
