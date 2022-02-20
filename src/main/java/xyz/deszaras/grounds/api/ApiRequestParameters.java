package xyz.deszaras.grounds.api;

/**
 * Reserved API request parameters.
 */
public final class ApiRequestParameters {

  /**
   * An API request parameter holding a plugin call ID.
   */
  public static final String PLUGIN_CALL_ID = "_plugin_call_id";
  /**
   * An API request parameter holding an extension ID.
   */
  public static final String EXTENSION_ID = "_extension_id";
  /**
   * An API request parameter holding plugin call arguments.
   */
  public static final String PLUGIN_CALL_ARGUMENTS = "_plugin_call_arguments";

  private ApiRequestParameters() {
  }
}
