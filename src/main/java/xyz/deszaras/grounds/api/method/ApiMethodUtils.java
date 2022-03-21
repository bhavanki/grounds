package xyz.deszaras.grounds.api.method;

import java.util.List;

import xyz.deszaras.grounds.api.JsonRpcRequest;

/**
 * Useful functions for implementing API methods.
 */
public final class ApiMethodUtils {

  private ApiMethodUtils() {
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
