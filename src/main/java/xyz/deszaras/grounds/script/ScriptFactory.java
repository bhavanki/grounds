package xyz.deszaras.grounds.script;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * A factory for {@link Script} objects.
 */
public class ScriptFactory {

  private static final String CONTENT = "scriptContent";

  private static final String HELP = "scriptHelp";

  /**
   * Creates a new script object from a script attribute. The attribute
   * must be of type ATTRLIST. Valid attrs in the list value are:<p>
   * <ul>
   * <li>"scriptContent" string attribute containing script content</li>
   * <li>"help" attrlist attribute containing help text</li>
   * </ul>
   *
   * @param scriptAttr script attribute
   * @param scriptExtension script extension
   * @return new script object
   * @throws ScriptFactoryException if the script attribute is the wrong
   * type, or is missing a scriptContent attribute in its list value, or has
   * a missing or invalid owner
   */
  public Script newScript(Attr scriptAttr, Extension scriptExtension) throws ScriptFactoryException {
    if (scriptAttr.getType() != Attr.Type.ATTRLIST) {
      throw new ScriptFactoryException("scriptAttr must be of type ATTRLIST" +
          " but is of type " + scriptAttr.getType());
    }
    List<Attr> attrs = scriptAttr.getAttrListValue();

    // The script's owner is the extension's owner.
    Thing scriptOwner;
    try {
      scriptOwner = scriptExtension.getOwner().orElseThrow(() ->
          new ScriptFactoryException("Cannot build script, extension " +
                                     scriptExtension.getName() + " has no owner"));
    } catch (MissingThingException e) {
      throw new ScriptFactoryException("Cannot build script, extension " +
                                       scriptExtension.getName() + " has missing owner!");
    }

    if (!(scriptOwner instanceof Player)) {
      throw new ScriptFactoryException("Cannot build script, extension " +
                                       scriptExtension.getName() + " has a non-player owner");
    }

    // Get the script from the scriptContent attribute.
    Optional<Attr> contentAttr = attrs.stream()
        .filter(a -> a.getName().equals(CONTENT) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    if (contentAttr.isEmpty()) {
      throw new ScriptFactoryException("Script attribute is missing scriptContent");
    }

    // Get the help text from the scriptHelp attribute.
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

    return new Script(contentAttr.get().getValue(), helpBundle,
                      (Player) scriptOwner, scriptExtension);
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
