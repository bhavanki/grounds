package xyz.deszaras.grounds.server;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

public class ShellTest {

  private Player player;

  @Before
  public void setUp() {
    Universe universe = new Universe("test");
    player = new Player("testPlayer", universe);
  }

  @Test
  public void testPreprocessNothing() {
    String line = "Hi there.";
    assertEquals(line, Shell.preprocess(line, player));
  }

  @Test
  public void testPreprocessPose() {
    String line = ":dances.";
    assertEquals("POSE " + player.getName() + " dances.",
                 Shell.preprocess(line, player));
  }

  @Test
  public void testPreprocessEmpty() {
    String line = "";
    assertEquals(line, Shell.preprocess(line, player));
  }

}
