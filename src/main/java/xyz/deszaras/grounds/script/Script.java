package xyz.deszaras.grounds.script;

import java.util.Objects;
import java.util.ResourceBundle;

import xyz.deszaras.grounds.model.Extension;

/**
 * A script to be run by the application. It could be the implementation
 * of a command, or the content of a listener attribute on a thing.
 */
public class Script {

  private final String content;
  private final ResourceBundle helpBundle;
  private final Extension extension;

  /**
   * Creates a new script.
   *
   * @param content script content
   * @param helpBundle resource bundle with help text
   * @param extension extension where script is stored as an attribute
   * @throws NullPointerException if any argument is null
   */
  public Script(String content, ResourceBundle helpBundle, Extension extension) {
    this.content = Objects.requireNonNull(content);
    this.helpBundle = Objects.requireNonNull(helpBundle);
    this.extension = Objects.requireNonNull(extension);
  }

  /**
   * Gets the script content.
   *
   * @return content
   */
  public String getContent() {
    return content;
  }

  /**
   * Gets the script help resource bundle.
   *
   * @return help resource bundle
   */
  public ResourceBundle getHelpBundle() {
    return helpBundle;
  }

  /**
   * Gets the script's extension.
   *
   * @return extension
   */
  public Extension getExtension() {
    return extension;
  }
}
