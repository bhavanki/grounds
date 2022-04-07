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

public class GetRolesMethodTest extends ApiMethodTest {

  private GetRolesMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new GetRolesMethod();
  }

  @Test
  public void testCall() throws Exception {
    mockSuccessfulCommand(caller, "Roles for caller: BARD,ADEPT");
    List<String> commandLine = List.of("ROLE", "GET", "thingId");
    request = new JsonRpcRequest("getRoles", Map.<String, Object>of(
        "thingId", "thingId"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(List.of("BARD", "ADEPT"), result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallPlayerName() throws Exception {
    mockSuccessfulCommand(caller, "Roles for caller: BARD,ADEPT");
    List<String> commandLine = List.of("ROLE", "GET", caller.getId().toString());
    request = new JsonRpcRequest("getRoles", Map.<String, Object>of(
        "playerName", "caller"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(List.of("BARD", "ADEPT"), result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, caller, commandLine);
  }

  @Test
  public void testCallAsExtension() throws Exception {
    mockSuccessfulCommand(extension, "Roles for caller: BARD,ADEPT");
    List<String> commandLine = List.of("ROLE", "GET", "thingId");
    request = new JsonRpcRequest("getRoles", Map.<String, Object>of(
        "thingId", "thingId",
        "_as_extension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(List.of("BARD", "ADEPT"), result);

    verify(commandExecutor.getCommandFactory())
        .getCommand(actor, extension, commandLine);
  }

  @Test
  public void testCallMissingThingIdAndPlayerName() throws Exception {
    request = new JsonRpcRequest("getRoles", Map.<String, Object>of());

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
    assertEquals("Either thingId or playerName is required", error.getMessage());
  }

  @Test
  public void testCallMissingPlayer() throws Exception {
    request = new JsonRpcRequest("getRoles", Map.<String, Object>of(
        "playerName", "someoneelse"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.NOT_FOUND);
    assertTrue(error.getMessage().contains("Player someoneelse not found"));
  }

  @Test
  public void testCallOtherError() throws Exception {
    mockFailedCommand(caller, "oopsie");
    request = new JsonRpcRequest("getAttr", Map.<String, Object>of(
        "thingId", "thingId"
    ));

    response = method.call(request, ctx);

    JsonRpcResponse.ErrorObject error =
        verifyError(JsonRpcErrorCodes.INTERNAL_ERROR);
    assertTrue(error.getMessage().contains("oopsie"));
  }
}
