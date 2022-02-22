package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.method.ApiMethod;
import xyz.deszaras.grounds.api.method.ApiMethodContext;
import xyz.deszaras.grounds.api.method.ApiMethodFactory;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ApiHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String REQUEST_ID = "rid1";
  private static final String PLUGIN_CALL_ID = "pcid1";

  private TestByteChannel channel;
  private PluginCallTracker pluginCallTracker;
  private ApiMethodFactory apiMethodFactory;
  private CommandExecutor commandExecutor;

  private Actor actor;
  private Player caller;
  private Extension extension;

  private ApiHandler handler;

  @BeforeEach
  public void setUp() throws Exception {
    pluginCallTracker = new PluginCallTracker();
    actor = mock(Actor.class);
    caller = mock(Player.class);
    extension = mock(Extension.class);
    pluginCallTracker.track(PLUGIN_CALL_ID,
                            new PluginCallTracker.PluginCallInfo(actor, caller, extension));

    apiMethodFactory = mock(ApiMethodFactory.class);
    commandExecutor = mock(CommandExecutor.class);
  }

  private static class PingPongApiMethod implements ApiMethod {
    JsonRpcRequest request;
    ApiMethodContext ctx;

    @Override
    public JsonRpcResponse call(JsonRpcRequest request, ApiMethodContext ctx) {
      this.request = request;
      this.ctx = ctx;
      return new JsonRpcResponse("pong", request.getId());
    }
  }

  @Test
  public void testSuccess() throws Exception {
    JsonRpcRequest req =
        new JsonRpcRequest("ping",
                           ImmutableMap.of(ApiRequestParameters.PLUGIN_CALL_ID,
                                           PLUGIN_CALL_ID),
                           REQUEST_ID);
    channel = new TestByteChannel(OBJECT_MAPPER.writeValueAsString(req));
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);
    PingPongApiMethod apiMethod = new PingPongApiMethod();
    when(apiMethodFactory.getApiMethod("ping"))
        .thenReturn(Optional.of(apiMethod));

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertEquals("pong", res.getResult());
    assertNull(res.getError());
    assertEquals(REQUEST_ID, apiMethod.request.getId());

    assertEquals("ping", apiMethod.request.getMethod());
    assertEquals(Optional.of(PLUGIN_CALL_ID),
                 apiMethod.request.getStringParam(ApiRequestParameters.PLUGIN_CALL_ID));
    assertEquals(REQUEST_ID, apiMethod.request.getId());

    assertEquals(actor, apiMethod.ctx.getActor());
    assertEquals(caller, apiMethod.ctx.getCaller());
    assertEquals(commandExecutor, apiMethod.ctx.getCommandExecutor());

    assertFalse(channel.isOpen());
  }

  @Test
  public void testInvalidJson() throws Exception {
    channel = new TestByteChannel("potato");
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertNull(res.getResult());
    assertEquals(JsonRpcErrorCodes.PARSE_ERROR, res.getError().getCode());
  }

  @Test
  public void testMissingJsonRpcId() throws Exception {
    JsonRpcRequest req =
        new JsonRpcRequest("ping",
                           ImmutableMap.of(ApiRequestParameters.PLUGIN_CALL_ID,
                                           PLUGIN_CALL_ID),
                           null);
    channel = new TestByteChannel(OBJECT_MAPPER.writeValueAsString(req));
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertNull(res.getResult());
    assertEquals(JsonRpcErrorCodes.INVALID_REQUEST, res.getError().getCode());
    assertEquals(ApiHandler.ERROR_MISSING_JSON_RPC_ID, res.getError().getMessage());
  }

  @Test
  public void testMissingPluginCallId() throws Exception {
    JsonRpcRequest req =
        new JsonRpcRequest("ping",
                           null,
                           REQUEST_ID);
    channel = new TestByteChannel(OBJECT_MAPPER.writeValueAsString(req));
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertNull(res.getResult());
    assertEquals(JsonRpcErrorCodes.INVALID_PARAMETERS, res.getError().getCode());
    assertEquals(ApiHandler.ERROR_MISSING_PLUGIN_CALL_ID, res.getError().getMessage());
    assertEquals(REQUEST_ID, res.getId());
  }

  @Test
  public void testUnknownPluginCallId() throws Exception {
    JsonRpcRequest req =
        new JsonRpcRequest("ping",
                           ImmutableMap.of(ApiRequestParameters.PLUGIN_CALL_ID,
                                           PLUGIN_CALL_ID + "xxx"),
                           REQUEST_ID);
    channel = new TestByteChannel(OBJECT_MAPPER.writeValueAsString(req));
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertNull(res.getResult());
    assertEquals(JsonRpcErrorCodes.INVALID_PARAMETERS, res.getError().getCode());
    assertEquals(String.format(ApiHandler.ERROR_UNKNOWN_PLUGIN_CALL_ID_FORMAT, PLUGIN_CALL_ID + "xxx"),
                 res.getError().getMessage());
    assertEquals(REQUEST_ID, res.getId());
  }

  @Test
  public void testUnknownMethod() throws Exception {
    JsonRpcRequest req =
        new JsonRpcRequest("ping",
                           ImmutableMap.of(ApiRequestParameters.PLUGIN_CALL_ID,
                                           PLUGIN_CALL_ID),
                           REQUEST_ID);
    channel = new TestByteChannel(OBJECT_MAPPER.writeValueAsString(req));
    handler = new ApiHandler(channel, pluginCallTracker,
                             apiMethodFactory, commandExecutor);
    when(apiMethodFactory.getApiMethod("ping"))
        .thenReturn(Optional.empty());

    handler.run();

    JsonRpcResponse res = OBJECT_MAPPER.readValue(channel.getOutput(),
                                                  JsonRpcResponse.class);
    assertNull(res.getResult());
    assertEquals(JsonRpcErrorCodes.METHOD_NOT_FOUND, res.getError().getCode());
    assertEquals(String.format(ApiHandler.ERROR_UNKNOWN_METHOD_FORMAT, "ping"),
                 res.getError().getMessage());
    assertEquals(REQUEST_ID, res.getId());
  }
}
