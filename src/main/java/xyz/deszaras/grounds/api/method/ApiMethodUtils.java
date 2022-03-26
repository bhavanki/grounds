package xyz.deszaras.grounds.api.method;

import java.util.List;
import java.util.Optional;

import xyz.deszaras.grounds.api.JsonRpcRequest;

/**
 * Useful functions for implementing API methods.
 */
public final class ApiMethodUtils {

  private ApiMethodUtils() {
  }

  /**
   * Gets a request param as a specified type. This is not type safe when T is
   * a generic type, so be careful.
   *
   * @param  request      request
   * @param  name         param name
   * @param  type         expected type of value
   * @param  defaultValue value if param is not present
   * @return              param value
   * @throws IllegalArgumentException if the param is not of the specified type
   */
  public static <T> T getParam(JsonRpcRequest request, String name, Class<T> type, T defaultValue) {
    Optional<Object> p = request.getParam(name);
    if (p.isEmpty()) {
      return defaultValue;
    }
    try {
      return type.cast(p.get());
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not of type " + type);
    }
  }

  /**
   * Gets a request param as a boolean.
   *
   * @param  request      request
   * @param  name         param name
   * @param  defaultValue value if param is not present
   * @return              param value
   * @throws IllegalArgumentException if the param is not a boolean
   */
  public static boolean getBooleanParam(JsonRpcRequest request, String name, boolean defaultValue) {
    try {
      return request.getBooleanParam(name).orElse(defaultValue);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a Boolean");
    }
  }

  /**
   * Gets a required request param as a boolean.
   *
   * @param  request      request
   * @param  name         param name
   * @return              param value
   * @throws IllegalArgumentException if the param is missing or not a boolean
   */
  public static boolean getRequiredBooleanParam(JsonRpcRequest request, String name) {
    try {
      return request.getBooleanParam(name)
          .orElseThrow(() -> new IllegalArgumentException("Parameter " + name + " missing"));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a Boolean");
    }
  }

  /**
   * Gets a request param as a string.
   *
   * @param  request      request
   * @param  name         param name
   * @param  defaultValue value if param is not present
   * @return              param value
   * @throws IllegalArgumentException if the param is not a string
   */
  public static String getStringParam(JsonRpcRequest request, String name, String defaultValue) {
    try {
      return request.getStringParam(name).orElse(defaultValue);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a string");
    }
  }

  /**
   * Gets a required request param as a string.
   *
   * @param  request      request
   * @param  name         param name
   * @return              param value
   * @throws IllegalArgumentException if the param is missing or not a string
   */
  public static String getRequiredStringParam(JsonRpcRequest request, String name) {
    try {
      return request.getStringParam(name)
          .orElseThrow(() -> new IllegalArgumentException("Parameter " + name + " missing"));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a string");
    }
  }

  /**
   * Gets a request param as a string list.
   *
   * @param  request      request
   * @param  name         param name
   * @param  defaultValue value if param is not present
   * @return              param value
   * @throws IllegalArgumentException if the param is not a string list
   */
  public static List<String> getStringListParam(JsonRpcRequest request, String name, List<String> defaultValue) {
    try {
      return request.getStringListParam(name).orElse(defaultValue);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a string list");
    }
  }

  /**
   * Gets a required request param as a string list.
   *
   * @param  request      request
   * @param  name         param name
   * @return              param value
   * @throws IllegalArgumentException if the param is missing or not a string list
   */
  public static List<String> getRequiredStringListParam(JsonRpcRequest request, String name) {
    try {
      return request.getStringListParam(name)
          .orElseThrow(() -> new IllegalArgumentException("Parameter " + name + " missing"));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Parameter " + name + " is not a string list");
    }
  }
}
