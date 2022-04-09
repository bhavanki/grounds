package xyz.deszaras.grounds.api;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;

/**
 * A call to be made to a plugin by the application. It could be the
 * implementation of a command, or the content of a listener attribute on a
 * thing.
 */
public class PluginCall {

  private final String pluginPath;
  private final String method;
  private final Set<Role> callerRoles;
  private final ResourceBundle helpBundle;
  private final Extension extension;
  private final PluginCallTracker pluginCallTracker;

  /**
   * Creates a new plugin call.
   *
   * @param  pluginPath        plugin path
   * @param  method            method to call in request to plugin
   * @param  callerRoles       permitted caller roles for plugin
   * @param  helpBundle        resource bundle with help text
   * @param  extension         extension where plugin call is stored as an attribute
   * @param  pluginCallTracker plugin call tracker
   * @throws NullPointerException if any argument is null
   */
  public PluginCall(String pluginPath, String method, Set<Role> callerRoles,
      ResourceBundle helpBundle, Extension extension, PluginCallTracker pluginCallTracker) {
    this.pluginPath = Objects.requireNonNull(pluginPath);
    this.method = Objects.requireNonNull(method);
    this.callerRoles = ImmutableSet.copyOf(Objects.requireNonNull(callerRoles));
    this.helpBundle = Objects.requireNonNull(helpBundle);
    this.extension = Objects.requireNonNull(extension);
    this.pluginCallTracker = Objects.requireNonNull(pluginCallTracker);
  }

  /**
   * Gets the plugin path.
   *
   * @return plugin path
   */
  public String getPluginPath() {
    return pluginPath;
  }

  /**
   * Gets the method.
   *
   * @return method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Gets the caller roles.
   *
   * @return caller roles
   */
  public Set<Role> getCallerRoles() {
    return callerRoles;
  }

  /**
   * Gets the plugin call help resource bundle.
   *
   * @return help resource bundle
   */
  public ResourceBundle getHelpBundle() {
    return helpBundle;
  }

  /**
   * Gets the plugin call's extension.
   *
   * @return extension
   */
  public Extension getExtension() {
    return extension;
  }

  /**
   * Gets the plugin call tracker.
   *
   * @return plugin call tracker
   */
  public PluginCallTracker getPluginCallTracker() {
    return pluginCallTracker;
  }

  @Override
  public String toString() {
    return String.format("%s::%s", pluginPath, method);
  }
}
