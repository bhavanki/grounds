package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                                             "key3", List.of("value3a", "value3b")),
                             "id1");

    assertEquals("2.0", req.getJsonrpc());
    assertEquals("method1", req.getMethod());
    assertEquals("id1", req.getId());

    assertEquals("value1", req.getParameter("key1").get());
    assertEquals("value2", req.getParameter("key2").get());
    assertEquals(List.of("value3a", "value3b"), req.getParameter("key3").get());

    assertEquals("value1", req.getStringParameter("key1").get());
    assertEquals(List.of("value3a", "value3b"), req.getStringListParameter("key3").get());

    Map params = req.getParameters();
    assertEquals(3, params.size());
    assertEquals("value1", params.get("key1"));
    assertEquals("value2", params.get("key2"));
    assertEquals(List.of("value3a", "value3b"), params.get("key3"));
  }

  @Test
  public void testSimpleConstructor() {
    req = new JsonRpcRequest("method",
                             ImmutableMap.of("key1", "value1",
                                             "key2", "value2",
                                             "key3", List.of("value3a", "value3b")));
    assertNotNull(req.getId());
  }

  @Test
  public void testJson() throws Exception {
    req = new JsonRpcRequest("method1",
                             ImmutableMap.of("key1", "value1",
                                             "key2", "value2",
                                             "key3", List.of("value3a", "value3b")),
                             "id1");
    String json = OBJECT_MAPPER.writeValueAsString(req);

    JsonRpcRequest req2 = OBJECT_MAPPER.readValue(json, JsonRpcRequest.class);

    assertEquals(req.getMethod(), req2.getMethod());
    assertEquals(req.getParameters(), req2.getParameters());
    assertEquals(req.getId(), req2.getId());
  }
}
