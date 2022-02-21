package xyz.deszaras.grounds.api;

/**
 * Reserved plugin call request parameters.
 */
public final class PluginCallRequestParameters {

  /**
   * A plugin call request parameter holding a plugin call ID.
   */
  public static final String PLUGIN_CALL_ID = ApiRequestParameters.PLUGIN_CALL_ID;
  /**
   * A plugin call request parameter holding an extension ID.
   */
  public static final String EXTENSION_ID = "_extension_id";
  /**
   * A plugin call request parameter holding plugin call arguments.
   */
  public static final String PLUGIN_CALL_ARGUMENTS = "_plugin_call_arguments";

  private PluginCallRequestParameters() {
  }
}
