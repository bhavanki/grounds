package xyz.deszaras.grounds.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

// https://www.jsonrpc.org/specification
/**
 * A JSON RPC 2.0 response. Result values must be strings.
 */
public class JsonRpcResponse {

  private static final String JSON_RPC_VERSION = "2.0";

  private final String result;
  private final ErrorObject error;
  private final String id;

  /**
   * Creates a new response.
   *
   * @param  jsonrpc JSON RPC version
   * @param  result  response result (optional if error is specified)
   * @param  error   response error (optional if result is specified)
   * @param  id      request ID (optional)
   */
  @JsonCreator
  public JsonRpcResponse(
    @JsonProperty(value="jsonrpc", required=true) String jsonrpc,
    @JsonProperty("result") String result,
    @JsonProperty("error") ErrorObject error,
    @JsonProperty("id") String id) {
    if (!JSON_RPC_VERSION.equals(jsonrpc)) {
      throw new IllegalArgumentException("Invalid jsonrpc value " + jsonrpc);
    }
    this.result = result;
    this.error = error;
    if ((result == null) == (error == null)) {
      throw new IllegalArgumentException("Exactly one of result or error must be given");
    }
    this.id = id;
  }

  /**
   * Creates a new response.
   *
   * @param  result  response result (optional if error is specified)
   * @param  error   response error (optional if result is specified)
   * @param  id      request ID (optional)
   */
  public JsonRpcResponse(String result, ErrorObject error, String id) {
    this(JSON_RPC_VERSION, result, error, id);
  }

  /**
   * Gets this responses's JSON RPC version.
   *
   * @return JSON RPC version
   */
  @JsonProperty
  public String getJsonrpc() {
    return JSON_RPC_VERSION;
  }

  /**
   * Gets this response's result.
   *
   * @return response result
   */
  @JsonProperty
  public String getResult() {
    return result;
  }

  /**
   * Gets this response's error.
   *
   * @return response error
   */
  @JsonProperty
  public ErrorObject getError() {
    return error;
  }

  /**
   * Gets this response's ID.
   *
   * @return response id
   */
  @JsonProperty
  public String getId() {
    return id;
  }

  /**
   * Error information for a response.
   */
  public static class ErrorObject {

    private final int code;
    private final String message;

    /**
     * Creates a new error.
     *
     * @param  code    error code
     * @param  message error message (optional)
     */
    @JsonCreator
    public ErrorObject(
      @JsonProperty("code") int code,
      @JsonProperty("message") String message) {
      this.code = code;
      this.message = message;
    }

    /**
     * Gets this error's code.
     *
     * @return error code
     */
    @JsonProperty
    public int getCode() {
      return code;
    }

    /**
     * Gets this error's message.
     *
     * @return error message
     */
    @JsonProperty
    public String getMessage() {
      return message;
    }
  }
}
