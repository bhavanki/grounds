package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.api.JsonRpcErrorCodes;
import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.command.Message;

public class SendMessageMethodTest extends ApiMethodTest {

  private SendMessageMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new SendMessageMethod();
  }

  @Test
  public void testCallMessage() throws Exception {
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller",
        "message", "Hello there"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(caller).sendMessage(msgCaptor.capture());
    Message msg = msgCaptor.getValue();
    assertEquals(caller, msg.getSender());
    assertEquals(Message.Style.SCRIPT, msg.getStyle());
    assertEquals("Hello there", msg.getMessage());
  }

  @Test
  public void testCallMessageHeader() throws Exception {
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller",
        "message", "Hello there",
        "header", "INCOMING JEDI MESSAGE"
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(caller).sendMessage(msgCaptor.capture());
    Message msg = msgCaptor.getValue();
    assertEquals("INCOMING JEDI MESSAGE\nHello there", msg.getMessage());
  }

  @Test
  public void testCallMessageAsExtension() throws Exception {
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller",
        "message", "Hello there",
        "asExtension", true
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(caller).sendMessage(msgCaptor.capture());
    Message msg = msgCaptor.getValue();
    assertEquals(extension, msg.getSender());
  }

  @Test
  public void testCallRecord() throws Exception {
    Map<String, List<String>> recordMap = Map.of(
      "keys", List.of("key1", "key2"),
      "values", List.of("value1", "value2")
    );
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller",
        "record", recordMap
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(caller).sendMessage(msgCaptor.capture());
    Message msg = msgCaptor.getValue();
    assertTrue(msg.getMessage().contains("value1"));
  }

  @Test
  public void testCallTable() throws Exception {
    Map<String, List<List<String>>> tableMap = Map.of(
      "columns", List.of(
        List.of("headera", "%s"),
        List.of("headerb", "%s")
      ),
      "rows", List.of(
        List.of("row1a", "row1b"),
        List.of("row2a", "row2b")
      )
    );
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller",
        "table", tableMap
    ));

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("", result);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(caller).sendMessage(msgCaptor.capture());
    Message msg = msgCaptor.getValue();
    assertTrue(msg.getMessage().contains("row1a"));
  }

  @Test
  public void testCallMissingPlayerName() throws Exception {
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "message", "Hello there"
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }

  @Test
  public void testCallMissingContent() throws Exception {
    request = new JsonRpcRequest("sendMessage", Map.<String, Object>of(
        "playerName", "caller"
    ));

    response = method.call(request, ctx);

    verifyError(JsonRpcErrorCodes.INVALID_PARAMETERS);
  }
}
