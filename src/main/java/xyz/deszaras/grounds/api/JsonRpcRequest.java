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
  private final Map<String, Object> parameters;
  private final String id;

  /**
   * Creates a new request.
   *
   * @param  jsonrpc    JSON RPC version (must be 2.0)
   * @param  method     request method
   * @param  parameters request parameters
   * @param  id         request ID (optional)
   */
  @JsonCreator
  public JsonRpcRequest(
    @JsonProperty(value="jsonrpc", required=true) String jsonrpc,
    @JsonProperty(value="method", required=true) String method,
    @JsonProperty("parameters") Map<String, Object> parameters,
    @JsonProperty("id") String id) {
    if (!JSON_RPC_VERSION.equals(jsonrpc)) {
      throw new IllegalArgumentException("Invalid jsonrpc value " + jsonrpc);
    }
    this.method = Objects.requireNonNull(method);
    this.parameters = parameters != null ?
        new HashMap<>(parameters) : new HashMap<>();
    this.id = id;
  }

  /**
   * Creates a new request.
   *
   * @param  method     request method
   * @param  parameters request parameters
   * @param  id         request ID (optional)
   */
  public JsonRpcRequest(String method, Map<String, Object> parameters,
                        String id) {
    this(JSON_RPC_VERSION, method, parameters, id);
  }

  /**
   * Creates a new request with a random, non-null request ID.
   *
   * @param  method     request method
   * @param  parameters request parameters
   */
  public JsonRpcRequest(String method, Map<String, Object> parameters) {
    this(method, parameters, UUID.randomUUID().toString());
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
  public Map<String, Object> getParameters() {
    return ImmutableMap.copyOf(parameters);
  }

  /**
   * Gets a parameter value from this request.
   *
   * @param  name parameter name
   * @return      parameter value
   */
  @JsonIgnore
  public Optional<Object> getParameter(String name) {
    return Optional.ofNullable(parameters.get(name));
  }

  /**
   * Gets a parameter value from this request as a string.
   *
   * @param  name parameter name
   * @return      parameter value
   * @throws ClassCastException if the value is not a string
   */
  @JsonIgnore
  public Optional<String> getStringParameter(String name) {
    return Optional.ofNullable(parameters.get(name))
        .map(o -> {
          if (!(o instanceof String)) {
            throw new ClassCastException("Value of parameter " + name + " is not a string");
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
  public Optional<List<String>> getStringListParameter(String name) {
    return Optional.ofNullable(parameters.get(name))
        .map(o -> {
          if (!(o instanceof List)) {
            throw new ClassCastException("Value of parameter " + name + " is not a list");
          }
          ImmutableList.Builder<String> lb = ImmutableList.<String>builder();
          for (Object e : (List) o) {
            if (!(e instanceof String)) {
              throw new ClassCastException("Element of parameter " + name + " is not a string");
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
  public Optional<Boolean> getBooleanParameter(String name) {
    return Optional.ofNullable(parameters.get(name))
        .map(o -> {
          if (!(o instanceof Boolean)) {
            throw new ClassCastException("Value of parameter " + name + " is not a Boolean");
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
  public String getId() {
    return id;
  }
}
