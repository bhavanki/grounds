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
        .put("getAttr", new GetAttrMethod())
        .put("getAttrNames", new GetAttrNamesMethod())
        .put("getCallerName", new GetCallerNameMethod())
        .put("getCallerTimezone", new GetCallerTimezoneMethod())
        .put("getRoles", new GetRolesMethod())
        .put("sendMessage", new SendMessageMethod())
        .put("sendMessageToCaller", new SendMessageToCallerMethod())
        .put("removeAttr", new RemoveAttrMethod())
        .put("setAttr", new SetAttrMethod())
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
