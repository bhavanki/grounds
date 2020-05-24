package xyz.deszaras.grounds.script;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * A factory for {@link Script} objects.
 */
public class ScriptFactory {

  /**
   * Creates a new script object from content.
   *
   * @param content script content
   * @param owner script owner
   * @return new script object
   * @throws NullPointerException if the script content is null
   */
  public Script newScript(String content, Player owner, Extension extension) {
    return new Script(content, owner, extension);
  }

  private static final String CONTENT = "scriptContent";

  /**
   * Creates a new script object from a script attribute. The attribute
   * must be of type ATTRLIST. Valid attrs in the list value are:<p>
   * <ul>
   * <li>"scriptContent" string attribute containing script content</li>
   * </ul>
   *
   * @param scriptAttr script attribute
   * @param owner script owner
   * @return new script object
   * @throws ScriptFactoryException if the script attribute is the wrong
   * type, or is missing a scriptContent attribute in its list value
   */
  public Script newScript(Attr scriptAttr, Player owner, Extension extension) throws ScriptFactoryException {
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

    return newScript(contentAttr.get().getValue(), owner, extension);
  }
}
