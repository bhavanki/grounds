package xyz.deszaras.grounds.api;

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;

/**
 * A factory for {@link PluginCall} objects.
 */
public class PluginCallFactory {

  private static final Splitter ROLE_SPLITTER = Splitter.on(",");

  private static final Set<Role> DEFAULT_CALLER_ROLES = Role.NON_GUEST_ROLES;

  private static final String PATH = "pluginPath";

  // FUTURE: plugin domain socket

  private static final String METHOD = "pluginMethod";

  private static final String CALLER_ROLES = "callerRoles";

  private static final String HELP = "commandHelp";

  /**
   * Creates a new plugin call object from a plugin call attribute. The
   * attribute must be of type ATTRLIST. Valid attrs in the list value are:<p>
   * <ul>
   * <li>"pluginPath" (string) = path to plugin to run</li>
   * <li>"pluginMethod" (string) = JSON-RPC method to call</li>
   * <li>"callerRoles" (string) = comma-separated list of allowed roles</li>
   * <li>"commandHelp" (attrlist) = help text</li>
   * </ul>
   *
   * @param pluginCallAttr    plugin call attribute
   * @param pluginExtension   plugin extension
   * @param pluginCallTracker plugin call tracker
   * @return new plugin call object
   * @throws PluginCallFactoryException if the plugin call attribute is the wrong
   * type, or is missing an attribute in its list value, or has
   * a missing or invalid owner
   */
  public PluginCall newPluginCall(Attr pluginCallAttr, Extension pluginExtension,
                                  PluginCallTracker pluginCallTracker)
      throws PluginCallFactoryException {
    if (pluginCallAttr.getType() != Attr.Type.ATTRLIST) {
      throw new PluginCallFactoryException("pluginCallAttr must be of type ATTRLIST" +
          " but is of type " + pluginCallAttr.getType());
    }
    List<Attr> attrs = pluginCallAttr.getAttrListValue();

    // Get the path from the pluginPath attribute.
    Optional<Attr> pathAttr = attrs.stream()
        .filter(a -> a.getName().equals(PATH) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    if (pathAttr.isEmpty()) {
      throw new PluginCallFactoryException("Plugin call attribute is missing " + PATH);
    }

    // Get the method from the pluginMethod attribute.
    Optional<Attr> methodAttr = attrs.stream()
        .filter(a -> a.getName().equals(METHOD) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    if (methodAttr.isEmpty()) {
      throw new PluginCallFactoryException("Plugin call attribute is missing " + METHOD);
    }

    // Get the permitted caller roles from the callerRoles attribute.
    Optional<Attr> callerRolesAttr = attrs.stream()
        .filter(a -> a.getName().equals(CALLER_ROLES) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    Set<Role> callerRoles;
    if (callerRolesAttr.isPresent()) {
      String callerRolesStr = callerRolesAttr.get().getValue();
      try {
        callerRoles = ROLE_SPLITTER.splitToList(callerRolesStr).stream()
            .map(s -> Role.valueOf(s))
            .collect(Collectors.toSet());
      } catch (IllegalArgumentException e) {
        throw new PluginCallFactoryException("At least one caller role is invalid: " + callerRolesStr);
      }
    } else {
      callerRoles = DEFAULT_CALLER_ROLES;
    }

    // Get the help text from the commandHelp attribute.
    Optional<Attr> helpAttr = attrs.stream()
        .filter(a -> a.getName().equals(HELP) &&
                     a.getType() == Attr.Type.ATTRLIST)
        .findFirst();
    ResourceBundle helpBundle;
    if (helpAttr.isPresent()) {
      helpBundle = new AttrListResourceBundle(helpAttr.get());
    } else {
      helpBundle = new EmptyResourceBundle();
    }

    return new PluginCall(pathAttr.get().getValue(),
                          methodAttr.get().getValue(),
                          callerRoles,
                          helpBundle,
                          pluginExtension,
                          pluginCallTracker);
  }

  /**
   * A resource bundle populated by an attribute with an attribute list
   * value. The name and value in each attribute of the attribute list value
   * becomes a key/value pair in the bundle.
   */
  private static class AttrListResourceBundle extends ListResourceBundle {

    private final Object[][] contents;

    private AttrListResourceBundle(Attr a) {
      Map<String, Attr> allHelp = a.getAttrListValueAsMap();
      List<Object[]> contentList = new ArrayList<>();

      for (Map.Entry<String, Attr> e : allHelp.entrySet()) {
        String cmdName = e.getKey();
        Attr helpText = e.getValue();
        for (Attr helpTextAttr : helpText.getAttrListValue()) {
          String bundleKey = cmdName + "." + helpTextAttr.getName();
          String bundleValue = helpTextAttr.getValue();
          contentList.add(new Object[] { bundleKey, bundleValue });
        }
      }

      contents = contentList.toArray(new Object[0][]);
    }

    @Override
    protected Object[][] getContents() {
      return contents;
    }
  }

  /**
   * A resource bundle with nothing in it.
   */
  private static class EmptyResourceBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
      return new Object[0][0];
    }
  }
}
