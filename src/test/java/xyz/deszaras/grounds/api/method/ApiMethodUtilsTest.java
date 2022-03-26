package xyz.deszaras.grounds.api.method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.JsonRpcRequest;

public class ApiMethodUtilsTest {

  private JsonRpcRequest request;

  @Test
  public void testGetParam() {
    request =
        new JsonRpcRequest("getParam",
                           Map.<String, Object>of("foo", "bar"));

    String value = ApiMethodUtils.getParam(request, "foo", String.class, "blat");
    assertEquals("bar", value);
  }

  @Test
  public void testGetParamDefaultValue() {
    request =
        new JsonRpcRequest("getParam", Map.<String, Object>of());

    String value = ApiMethodUtils.getParam(request, "foo", String.class, "blat");
    assertEquals("blat", value);
  }

  @Test
  public void testGetParamWrongType() {
    request =
        new JsonRpcRequest("getParam",
                           Map.<String, Object>of("foo", "bar"));

    assertThrows(IllegalArgumentException.class,
                 () -> ApiMethodUtils.getParam(request, "foo", Integer.class, 42));
  }

  @Test
  public void testGetParamGenerics() {
    List<String> paramValue = List.of("bar");
    List<String> defaultValue = List.of("blat");
    request =
        new JsonRpcRequest("getParam",
                           Map.<String, Object>of("foo", paramValue));

    List<String> value = ApiMethodUtils.getParam(request, "foo", List.class, defaultValue);
    assertEquals(paramValue, value);
  }

  @SuppressWarnings("PMD.UnusedLocalVariable")
  @Test
  public void testGetParamGenericsWrongTypeParam() {
    List<String> paramValue = List.of("bar");
    List<Integer> defaultValue = List.of(43);
    request =
        new JsonRpcRequest("getParam",
                           Map.<String, Object>of("foo", paramValue));

    List<Integer> value = ApiMethodUtils.getParam(request, "foo", List.class, defaultValue);
    assertThrows(ClassCastException.class,
                 () -> {
                   Integer firstValue = value.get(0);
                 });
  }
}
