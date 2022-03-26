package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.command.Message;

public class SendMessageToCallerMethodTest extends ApiMethodTest {

  private SendMessageToCallerMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new SendMessageToCallerMethod();
  }

  @Test
  public void testCall() throws Exception {
    request = new JsonRpcRequest("sendMessageToCaller", Map.<String, Object>of(
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
}
