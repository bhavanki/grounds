package xyz.deszaras.grounds.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

// https://www.jsonrpc.org/specification
/**
 * A JSON RPC 2.0 request. Parameter values may be either strings or lists of
 * strings.
 */
public class JsonRpcRequest {

  private static final String JSON_RPC_VERSION = "2.0";

  private final String method;
  private final Map<String, Object> params;
  private final Object id;

  /**
   * Creates a new request.
   *
   * @param  jsonrpc JSON RPC version (must be 2.0)
   * @param  method  request method
   * @param  params  request parameters
   * @param  id      request ID (optional)
   */
  @JsonCreator
  public JsonRpcRequest(
    @JsonProperty(value="jsonrpc", required=true) String jsonrpc,
    @JsonProperty(value="method", required=true) String method,
    @JsonProperty("params") Map<String, Object> params,
    @JsonProperty("id") Object id) {
    if (!JSON_RPC_VERSION.equals(jsonrpc)) {
      throw new IllegalArgumentException("Invalid jsonrpc value " + jsonrpc);
    }
    this.method = Objects.requireNonNull(method);
    this.params = params != null ?
        new HashMap<>(params) : new HashMap<>();
    if (id != null && !(id instanceof String || id instanceof Number)) {
      throw new IllegalArgumentException("id must be string or number");
  }
    this.id = id;
  }

  /**
   * Creates a new request.
   *
   * @param  method request method
   * @param  params request parameters
   * @param  id     request ID (optional)
   */
  public JsonRpcRequest(String method, Map<String, Object> params,
                        Object id) {
    this(JSON_RPC_VERSION, method, params, id);
  }

  /**
   * Creates a new request with a random, non-null request ID.
   *
   * @param  method request method
   * @param  params request parameters
   */
  public JsonRpcRequest(String method, Map<String, Object> params) {
    this(method, params, UUID.randomUUID().toString());
  }

  /**
   * Gets this request's JSON RPC version.
   *
   * @return JSON RPC version
   */
  @JsonProperty
  public String getJsonrpc() {
    return JSON_RPC_VERSION;
  }

  /**
   * Gets this request's method.
   *
   * @return request method
   */
  @JsonProperty
  public String getMethod() {
    return method;
  }

  /**
   * Gets this request's parameters.
   *
   * @return request parameters
   */
  @JsonProperty
  public Map<String, Object> getParams() {
    return ImmutableMap.copyOf(params);
  }

  /**
   * Gets a parameter value from this request.
   *
   * @param  name parameter name
   * @return      parameter value
   */
  @JsonIgnore
  public Optional<Object> getParam(String name) {
    return Optional.ofNullable(params.get(name));
  }

  /**
   * Gets a parameter value from this request as a string.
   *
   * @param  name parameter name
   * @return      parameter value
   * @throws ClassCastException if the value is not a string
   */
  @JsonIgnore
  public Optional<String> getStringParam(String name) {
    return Optional.ofNullable(params.get(name))
        .map(o -> {
          if (!(o instanceof String)) {
            throw new ClassCastException("Value of param " + name + " is not a string");
          }
          return (String) o;
        });
  }

  /**
   * Gets a parameter value from this request as a list of strings.
   *
   * @param  name parameter name
   * @return      parameter value
   * @throws ClassCastException if the value is not a list, or if any element is
   *                            not a string
   */
  @JsonIgnore
  public Optional<List<String>> getStringListParam(String name) {
    return Optional.ofNullable(params.get(name))
        .map(o -> {
          if (!(o instanceof List)) {
            throw new ClassCastException("Value of param " + name + " is not a list");
          }
          ImmutableList.Builder<String> lb = ImmutableList.<String>builder();
          for (Object e : (List) o) {
            if (!(e instanceof String)) {
              throw new ClassCastException("Element of param " + name + " is not a string");
            }
            lb.add((String) e);
          }
          return lb.build();
        });
  }

  /**
   * Gets a parameter value from this request as a Boolean.
   *
   * @param  name parameter name
   * @return      parameter value
   * @throws ClassCastException if the value is not a Boolean
   */
  @JsonIgnore
  public Optional<Boolean> getBooleanParam(String name) {
    return Optional.ofNullable(params.get(name))
        .map(o -> {
          if (!(o instanceof Boolean)) {
            throw new ClassCastException("Value of param " + name + " is not a Boolean");
          }
          return (Boolean) o;
        });
  }


  /**
   * Gets this request's ID.
   *
   * @return request ID (may be null)
   */
  @JsonProperty
  public Object getId() {
    return id;
  }
}
