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
   * An API request parameters indicating if the method should be run as the
   * calling plugin's extension.
   */
  public static final String AS_EXTENSION = "_as_extension";

  private ApiRequestParameters() {
  }
}
