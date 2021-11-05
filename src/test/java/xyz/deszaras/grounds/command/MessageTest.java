package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class MessageTest {

  private static final Message.Style TEST_STYLE = new Message.Style("** %s **");

  private Player p;
  private Message m;

  @BeforeEach
  public void setUp() {
    p = new Player("bob");
  }

  @Test
  public void testGetters() {
    m = new Message(p, TEST_STYLE, "hello");

    assertEquals(p, m.getSender());
    assertEquals(TEST_STYLE, m.getStyle());
    assertEquals("hello", m.getMessage());

    assertEquals("** hello **", m.getStyledMessage().toString());
  }

  @Test
  public void testExpandHorizontalRules() {
    m = new Message(p, TEST_STYLE, "hello\n{hr -}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\n----------\ngoodbye", m.getMessage());

    m = new Message(p, TEST_STYLE, "hello\n{hr -*}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\n-*-*-*-*-*\ngoodbye", m.getMessage());

    m = new Message(p, TEST_STYLE, "hello\n{hr <->}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\n<-><-><->\ngoodbye", m.getMessage());
  }

  @Test
  public void testExpandHorizontalRulesWithTitle() {
    m = new Message(p, TEST_STYLE, "hello\n{hr - foo}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\nfoo ------\ngoodbye", m.getMessage());

    m = new Message(p, TEST_STYLE, "hello\n{hr -* foop}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\nfoop *-*-*\ngoodbye", m.getMessage());

    m = new Message(p, TEST_STYLE, "hello\n{hr - iamtoolongforyou}\ngoodbye");
    m = m.expandHorizontalRules(10);
    assertEquals("hello\niamtoolong\ngoodbye", m.getMessage());
  }
}
