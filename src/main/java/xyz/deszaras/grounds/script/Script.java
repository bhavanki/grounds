package xyz.deszaras.grounds.script;

import java.util.Objects;
import java.util.ResourceBundle;

import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

/**
 * A script to be run by the application. It could be the implementation
 * of a command, or the content of a listener attribute on a thing.
 */
public class Script {

  private final String content;
  private final ResourceBundle helpBundle;
  private final Player owner;
  private final Extension extension;

  /**
   * Creates a new script.
   *
   * @param content script content
   * @param helpBundle resource bundle with help text
   * @param owner owner of script
   * @param extension extension where script is stored as an attribute
   * @throws NullPointerException if any argument is null
   */
  public Script(String content, ResourceBundle helpBundle, Player owner,
                Extension extension) {
    this.content = Objects.requireNonNull(content);
    this.helpBundle = Objects.requireNonNull(helpBundle);
    this.owner = Objects.requireNonNull(owner);
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
   * Gets the script owner.
   *
   * @return owner
   */
  public Player getOwner() {
    return owner;
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
