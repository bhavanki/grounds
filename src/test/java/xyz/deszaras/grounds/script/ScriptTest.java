package xyz.deszaras.grounds.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Player;

public class ScriptTest {

  @Test
  public void testGetters() {
    String content = "println('Hello world')";
    Player owner = new Player("owner");
    Extension extension = new Extension("extension");

    Script s = new Script(content, owner, extension);

    assertEquals(content, s.getContent());
    assertEquals(owner, s.getOwner());
    assertEquals(extension, s.getExtension());
  }

}
