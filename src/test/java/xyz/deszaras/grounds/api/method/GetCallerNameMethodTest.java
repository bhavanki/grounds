package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.JsonRpcRequest;

public class GetCallerNameMethodTest extends ApiMethodTest {

  private GetCallerNameMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new GetCallerNameMethod();
  }

  @Test
  public void testCall() {
    request = new JsonRpcRequest("getCallerName", Map.<String, Object>of());

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals(caller.getName(), result);
  }
}
