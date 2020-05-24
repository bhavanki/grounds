package xyz.deszaras.grounds.script;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.script.Script.ArgType;

/**
 * A factory for {@link Script} objects.
 */
public class ScriptFactory {

  private static final Splitter ARG_STRING_SPLITTER = Splitter.on(",");

  /**
   * Creates a new script object from content and an arg string, which
   * is a comma-separated list of script argument types.
   *
   * @param content script content
   * @param argString arg string (may be null for no arguments)
   * @return new script object
   * @throws ScriptFactoryException if the arg string cannot be parsed
   * @throws NullPointerException if the script content is null
   */
  public Script newScript(String content, String argString)
      throws ScriptFactoryException {
    List<ArgType> types = new ArrayList<>();
    if (argString != null && !argString.isEmpty()) {
      for (String typeString : ARG_STRING_SPLITTER.split(argString)) {
        try {
          types.add(ArgType.valueOf(typeString.toUpperCase()));
        } catch (IllegalArgumentException e) {
          throw new ScriptFactoryException("Failed to parse arg string for script", e);
        }
      }
    }
    return new Script(content, types);
  }

  private static final String CONTENT = "scriptContent";
  private static final String ARG_STRING = "scriptArgString";

  /**
   * Creates a new script object from a script attribute. The attribute
   * must be of type ATTRLIST. Valid attrs in the list value are:<p>
   * <ul>
   * <li>"scriptContent" string attribute containing script content</li>
   * <li>"scriptArgString" string attribute containing an arg string
   *     (optional)</li>
   * </ul>
   *
   * @param scriptAttr script attribute
   * @return new script object
   * @throws ScriptFactoryException if the script attribute is the wrong
   * type, or is missing a scriptContent attribute in its list value
   */
  public Script newScript(Attr scriptAttr) throws ScriptFactoryException {
    if (scriptAttr.getType() != Attr.Type.ATTRLIST) {
      throw new ScriptFactoryException("scriptAttr must be of type ATTRLIST" +
          " but is of type " + scriptAttr.getType());
    }
    List<Attr> attrs = scriptAttr.getAttrListValue();

    // Get the script from the script attribute.
    Optional<Attr> contentAttr = attrs.stream()
        .filter(a -> a.getName().equals(CONTENT) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    if (contentAttr.isEmpty()) {
      throw new ScriptFactoryException("Script attribute is missing scriptContent");
    }

    // Get the argString from the attribute list, if present.
    Optional<Attr> argStringAttr = attrs.stream()
        .filter(a -> a.getName().equals(ARG_STRING) &&
                     a.getType() == Attr.Type.STRING)
        .findFirst();
    String argString = argStringAttr.map(a -> a.getValue()).orElse(null);
    return newScript(contentAttr.get().getValue(), argString);
  }
}
