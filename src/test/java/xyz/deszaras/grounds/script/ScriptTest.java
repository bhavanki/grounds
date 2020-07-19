package xyz.deszaras.grounds.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

public class ScriptTest {

  @Test
  public void testGetters() {
    String content = "println('Hello world')";
    ResourceBundle helpBundle = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[0][];
      }
    };
    Player owner = new Player("owner");
    Extension extension = new Extension("extension");

    Script s = new Script(content, helpBundle, owner, extension);

    assertEquals(content, s.getContent());
    assertEquals(helpBundle, s.getHelpBundle());
    assertEquals(owner, s.getOwner());
    assertEquals(extension, s.getExtension());
  }

}
