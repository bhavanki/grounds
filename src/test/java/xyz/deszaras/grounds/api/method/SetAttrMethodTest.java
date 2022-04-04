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

public class SetAttrMethodTest extends ApiMethodTest {

  private Attr a;
  private SetAttrMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    a = new Attr("attrName", "attrValue");

    method = new SetAttrMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, true);
    List<String> commandLine = List.of("SET_ATTR", "thingId", a.toAttrSpec());
    request = new JsonRpcRequest("setAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", a.getName(),
        "value", a.getValue(),
        "type", a.getType().name()
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
    List<String> commandLine = List.of("SET_ATTR", "thingId", a.toAttrSpec());
    request = new JsonRpcRequest("setAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", a.getName(),
        "value", a.getValue(),
        "type", a.getType().name(),
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
    request = new JsonRpcRequest("setAttr", Map.<String, Object>of(
        "name", a.getName(),
        "value", a.getValue(),
        "type", a.getType().name()
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallMissingName() throws Exception {
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "value", a.getValue(),
        "type", a.getType().name()
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  // you get the idea

  @Test
  public void testCallInvalidType() throws Exception {
    request = new JsonRpcRequest("setAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", a.getName(),
        "value", a.getValue(),
        "type", "PURPLE"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
    assertEquals("Invalid type PURPLE", error.getMessage());
  }

  @Test
  public void testCallExecFailure() throws Exception {
    mockFailedCommand(caller, "oopsie");
    request = new JsonRpcRequest("setAttr", Map.<String, Object>of(
        "thingId", "thingId",
        "name", a.getName(),
        "value", a.getValue(),
        "type", a.getType().name()
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
