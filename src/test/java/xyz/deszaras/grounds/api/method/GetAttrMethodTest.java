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
import xyz.deszaras.grounds.model.Attr;

public class GetAttrMethodTest extends ApiMethodTest {

  private Attr a;
  private GetAttrMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    a = new Attr("attrName", "attrValue");

    method = new GetAttrMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, a.toAttrSpec());
    List<String> commandLine = List.of("GET_ATTR", "thingId", "attrName");
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(a, result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallAsExtension() throws Exception {
    mockSuccessfulCommand(extension, a.toAttrSpec());
    List<String> commandLine = List.of("GET_ATTR", "thingId", "attrName");
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName",
        "_as_extension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(a, result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, extension, commandLine);
  }

  @Test
  public void testCallMissingThingId() throws Exception {
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
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
  public void testCallMissingAttr() throws Exception {
    mockFailedCommand(caller, "There is no attribute");
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.NOT_FOUND);
    assertTrue(error.getMessage().contains("There is no attribute"));
  }

  @Test
  public void testCallOtherError() throws Exception {
    mockFailedCommand(caller, "oopsie");
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", "attrName"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
