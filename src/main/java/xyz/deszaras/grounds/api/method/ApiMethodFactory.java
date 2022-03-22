package xyz.deszaras.grounds.api.method;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

/**
 * A factory for {@link ApiMethod} instances.
 */
public class ApiMethodFactory {

  private static final Map<String, ApiMethod> METHODS;

  static {
    METHODS = ImmutableMap.<String, ApiMethod>builder()
        .put("exec", new ExecMethod())
        .put("getCallerName", new GetCallerNameMethod())
        .build();
  }

  /**
   * Gets a method instance based on the given method name.
   *
   * @param  method API method name
   * @return        APi method
   */
  public Optional<ApiMethod> getApiMethod(String method) {
    return Optional.ofNullable(METHODS.get(method));
  }
}
