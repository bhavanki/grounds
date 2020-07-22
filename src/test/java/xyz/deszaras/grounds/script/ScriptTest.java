package xyz.deszaras.grounds.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Extension;

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
    Extension extension = new Extension("extension");

    Script s = new Script(content, helpBundle, extension);

    assertEquals(content, s.getContent());
    assertEquals(helpBundle, s.getHelpBundle());
    assertEquals(extension, s.getExtension());
  }

}
