package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.JsonRpcRequest;
import xyz.deszaras.grounds.command.Actor;

public class GetCallerTimezoneMethodTest extends ApiMethodTest {

  private GetCallerTimezoneMethod method;

  @BeforeEach
  public void setUp() {
    super.setUp();

    method = new GetCallerTimezoneMethod();
  }

  @Test
  public void testCall() {
    actor.setPreference(Actor.PREFERENCE_TIMEZONE, "America/New_York");
    caller.setCurrentActor(actor);
    request = new JsonRpcRequest("getCallerTimezone", Map.<String, Object>of());

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("America/New_York", result);
  }

  @Test
  public void testCallNoActor() {
    caller.setCurrentActor(null);
    request = new JsonRpcRequest("getCallerTimezone", Map.<String, Object>of());

    response = method.call(request, ctx);

    Object result = verifySuccessfulResult();
    assertEquals("Z", result);
  }
}
