package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;
import xyz.deszaras.grounds.api.PluginCallTracker.PluginCallInfo;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

@SuppressWarnings("PMD.TooManyStaticImports")
public class PluginCallableTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private Actor actor;
  private Player player;
  private Extension extension;
  private PluginCallTracker tracker;
  private List<String> args;

  private Process process;
  private ByteArrayOutputStream stdin;
  private ByteArrayInputStream stdout;
  private JsonRpcRequest request;
  private JsonRpcResponse response;

  private PluginCall call;
  private PluginCallable callable;

  private static class TestPluginCallable extends PluginCallable {
    private final Process process;

    private TestPluginCallable(Actor actor, Player player, PluginCall call,
                               List<String> args, Process process) {
      super(actor, player, call, args);
      this.process = process;
    }

    @Override
    protected Process buildProcess(String path) {
      return process;
    }
  }

  @BeforeEach
  public void setUp() {
    actor = mock(Actor.class);
    player = Player.GOD;
    extension = new Extension("pluginExtension");
    tracker = mock(PluginCallTracker.class);
    args = List.of("arg1", "arg2", "arg3");

    process = mock(Process.class);
    stdin = new ByteArrayOutputStream();
    when(process.getOutputStream()).thenReturn(stdin);

    call = new PluginCall(PluginCallTest.PLUGIN_PATH,
                          PluginCallTest.PLUGIN_METHOD,
                          PluginCallTest.PLUGIN_CALLER_ROLES,
                          PluginCallTest.PLUGIN_HELP_BUNDLE,
                          extension,
                          tracker);

    callable = new TestPluginCallable(actor, player, call, args, process);
  }

  @Test
  public void testCall() throws Exception {
    mockResponse(new JsonRpcResponse("OK", "xxx"));
    when(process.waitFor()).thenReturn(0);

    assertEquals("OK", callable.call());

    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<PluginCallInfo> infoCaptor = ArgumentCaptor.forClass(PluginCallInfo.class);
    verify(tracker).track(idCaptor.capture(), infoCaptor.capture());
    String id = idCaptor.getValue();
    verify(tracker).untrack(id);
    PluginCallInfo info = infoCaptor.getValue();
    assertEquals(actor, info.getActor());
    assertEquals(player, info.getCaller());
    assertEquals(extension, info.getExtension());

    request = OBJECT_MAPPER.readValue(stdin.toByteArray(), JsonRpcRequest.class);
    assertEquals(PluginCallTest.PLUGIN_METHOD, request.getMethod());
    assertEquals(id, request.getStringParam(PluginCallRequestParameters.PLUGIN_CALL_ID).get());
    assertEquals(extension.getId().toString(),
                 request.getStringParam(PluginCallRequestParameters.EXTENSION_ID).get());
    assertEquals(args,
                 request.getStringListParam(PluginCallRequestParameters.PLUGIN_CALL_ARGUMENTS).get());

  }

  @Test
  public void testCallEmptyResult() throws Exception {
    mockResponse(new JsonRpcResponse("", "xxx"));
    when(process.waitFor()).thenReturn(0);

    assertNull(callable.call());
  }

  @Test
  public void testCallPermitted() throws Exception {
    Universe testUniverse = new Universe("test");
    Universe.setCurrent(testUniverse);
    player = new Player("player");
    testUniverse.addThing(player);
    testUniverse.addRole(Role.THAUMATURGE, player);

    callable = new TestPluginCallable(actor, player, call, args, process);

    mockResponse(new JsonRpcResponse("OK", "xxx"));
    when(process.waitFor()).thenReturn(0);

    assertEquals("OK", callable.call());
  }

  @Test
  public void testCallNotPermitted() throws Exception {
    Universe testUniverse = new Universe("test");
    Universe.setCurrent(testUniverse);
    player = new Player("player");
    testUniverse.addThing(player);
    testUniverse.addRole(Role.DENIZEN, player);

    callable = new TestPluginCallable(actor, player, call, args, process);

    assertThrows(PermissionException.class, () -> callable.call());
  }

  @Test
  public void testCallUnsuccessful() throws Exception {
    mockResponse(new JsonRpcResponse(new ErrorObject(404, "Not found"), "xxx"));
    when(process.waitFor()).thenReturn(0);

    CommandException e = assertThrows(CommandException.class,
                                      () -> callable.call());

    assertTrue(e.getMessage().contains("Not found"));
  }

  @Test
  public void testCallResultNonString() throws Exception {
    mockResponse(new JsonRpcResponse(200, "xxx"));
    when(process.waitFor()).thenReturn(0);

    assertThrows(CommandException.class, () -> callable.call());
  }

  private void mockResponse(JsonRpcResponse r) throws JsonProcessingException {
    response = r;
    byte[] responseBytes = OBJECT_MAPPER.writeValueAsBytes(response);
    stdout = new ByteArrayInputStream(responseBytes);
    when(process.getInputStream()).thenReturn(stdout);
  }
}
