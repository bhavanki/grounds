package xyz.deszaras.grounds.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// https://www.jsonrpc.org/specification
/**
 * A JSON RPC 2.0 response.
 */
public class JsonRpcResponse {

  private static final String JSON_RPC_VERSION = "2.0";

  private final Object result;
  private final ErrorObject error;
  private final Object id;

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
    @JsonProperty("result") Object result,
    @JsonProperty("error") ErrorObject error,
    @JsonProperty("id") Object id) {
    if (!JSON_RPC_VERSION.equals(jsonrpc)) {
      throw new IllegalArgumentException("Invalid jsonrpc value " + jsonrpc);
    }
    this.result = result;
    this.error = error;
    if ((result == null) == (error == null)) {
      throw new IllegalArgumentException("Exactly one of result or error must be given");
    }
    if (id != null && !(id instanceof String || id instanceof Number)) {
      throw new IllegalArgumentException("id must be string or number");
    }
    this.id = id;
  }

  /**
   * Creates a new successful response.
   *
   * @param  result  response result
   * @param  id      request ID (optional)
   */
  public JsonRpcResponse(Object result, Object id) {
    this(JSON_RPC_VERSION, result, null, id);
  }

  /**
   * Creates a new error response.
   *
   * @param  error   response error
   * @param  id      request ID (optional)
   */
  public JsonRpcResponse(ErrorObject error, Object id) {
    this(JSON_RPC_VERSION, null, error, id);
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
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Object getResult() {
    return result;
  }

  /**
   * Gets this response's error.
   *
   * @return response error
   */
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public ErrorObject getError() {
    return error;
  }

  /**
   * Gets this response's ID.
   *
   * @return response id
   */
  @JsonProperty
  public Object getId() {
    return id;
  }

  /**
   * Gets whether the response is successful, i.e., has a result.
   *
   * @return true for success
   */
  @JsonIgnore
  public boolean isSuccessful() {
    return result != null;
  }

  /**
   * Error information for a response.
   */
  @JsonIgnoreProperties(ignoreUnknown=true) // for data
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
