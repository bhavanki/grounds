package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class JsonRpcRequestTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JsonRpcRequest req;

  @Test
  public void testGetters() {
    req = new JsonRpcRequest("method1",
                             ImmutableMap.of("key1", "value1",
                                             "key2", "value2",
                                             "key3", List.of("value3a", "value3b"),
                                             "key4", true),
                             "id1");

    assertEquals("2.0", req.getJsonrpc());
    assertEquals("method1", req.getMethod());
    assertEquals("id1", req.getId());

    assertEquals("value1", req.getParam("key1").get());
    assertEquals("value2", req.getParam("key2").get());
    assertEquals(List.of("value3a", "value3b"), req.getParam("key3").get());
    assertEquals(true, req.getParam("key4").get());

    assertEquals("value1", req.getStringParam("key1").get());
    assertEquals(List.of("value3a", "value3b"), req.getStringListParam("key3").get());
    assertEquals(true, req.getBooleanParam("key4").get());

    Map params = req.getParams();
    assertEquals(4, params.size());
    assertEquals("value1", params.get("key1"));
    assertEquals("value2", params.get("key2"));
    assertEquals(List.of("value3a", "value3b"), params.get("key3"));
    assertEquals(true, params.get("key4"));
  }

  @Test
  public void testSimpleConstructor() {
    req = new JsonRpcRequest("method",
                             ImmutableMap.of("key1", "value1",
                                             "key2", "value2",
                                             "key3", List.of("value3a", "value3b"),
                                             "key4", true));
    assertNotNull(req.getId());
  }

  @Test
  public void testIntegerId() {
    req = new JsonRpcRequest("method1",
                             ImmutableMap.of("key1", "value1"),
                             1234);
    assertEquals(1234, ((Integer) req.getId()).intValue());
  }

  @Test
  public void testInvalidId() {
    assertThrows(IllegalArgumentException.class,
                 () -> new JsonRpcRequest("method1",
                                          ImmutableMap.of("key1", "value1"),
                                          Boolean.TRUE));
  }

  @Test
  public void testJson() throws Exception {
    req = new JsonRpcRequest("method1",
                             ImmutableMap.of("key1", "value1",
                                             "key2", "value2",
                                             "key3", List.of("value3a", "value3b"),
                                             "key4", true),
                             "id1");
    String json = OBJECT_MAPPER.writeValueAsString(req);

    JsonRpcRequest req2 = OBJECT_MAPPER.readValue(json, JsonRpcRequest.class);

    assertEquals(req.getMethod(), req2.getMethod());
    assertEquals(req.getParams(), req2.getParams());
    assertEquals(req.getId(), req2.getId());
  }
}
