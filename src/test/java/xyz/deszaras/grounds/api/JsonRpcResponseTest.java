package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.JsonRpcResponse.ErrorObject;

@SuppressWarnings("PMD.TooManyStaticImports")
public class JsonRpcResponseTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JsonRpcResponse res;

  @Test
  public void testGettersNoError() {
    res = new JsonRpcResponse("OK", "id1");

    assertEquals("2.0", res.getJsonrpc());
    assertEquals("OK", res.getResult());
    assertEquals("id1", res.getId());
    assertNull(res.getError());
    assertTrue(res.isSuccessful());
  }

  @Test
  public void testGettersError() {
    res = new JsonRpcResponse(new ErrorObject(123, "oops"), "id1");

    assertEquals("2.0", res.getJsonrpc());
    assertNull(res.getResult());
    assertEquals("id1", res.getId());
    assertEquals(123, res.getError().getCode());
    assertEquals("oops", res.getError().getMessage());
    assertFalse(res.isSuccessful());
  }

  @Test
  public void testNotBothResultAndError() {
    assertThrows(IllegalArgumentException.class,
                 () -> new JsonRpcResponse("2.0", "OK", new ErrorObject(123, "oops"), "id1"));
  }

  @Test
  public void testIntegerId() {
    res = new JsonRpcResponse("OK", 1234);

    assertEquals(1234, ((Integer) res.getId()).intValue());
  }

  @Test
  public void testInvalidId() {
    assertThrows(IllegalArgumentException.class,
                 () -> new JsonRpcResponse("OK", Boolean.TRUE));
  }

  @Test
  public void testJsonNoError() throws Exception {
    res = new JsonRpcResponse("OK", "id1");
    String json = OBJECT_MAPPER.writeValueAsString(res);

    JsonRpcResponse res2 = OBJECT_MAPPER.readValue(json, JsonRpcResponse.class);

    assertEquals(res.getResult(), res2.getResult());
    assertEquals(res.getId(), res2.getId());
    assertNull(res.getError());
  }

  @Test
  public void testJsonNoErrorWithMapResult() throws Exception {
    Map<String, String> result = ImmutableMap.of("foo", "bar");
    res = new JsonRpcResponse(result, "id1");
    String json = OBJECT_MAPPER.writeValueAsString(res);

    JsonRpcResponse res2 = OBJECT_MAPPER.readValue(json, JsonRpcResponse.class);

    assertEquals(result, res2.getResult());
    assertEquals(res.getId(), res2.getId());
    assertNull(res.getError());
  }

  @Test
  public void testJsonError() throws Exception {
    res = new JsonRpcResponse(new ErrorObject(123, "oops"), "id1");
    String json = OBJECT_MAPPER.writeValueAsString(res);

    JsonRpcResponse res2 = OBJECT_MAPPER.readValue(json, JsonRpcResponse.class);

    assertNull(res.getResult());
    assertEquals(res.getId(), res2.getId());
    assertEquals(res.getError().getCode(), res2.getError().getCode());
    assertEquals(res.getError().getMessage(), res2.getError().getMessage());
  }
}
