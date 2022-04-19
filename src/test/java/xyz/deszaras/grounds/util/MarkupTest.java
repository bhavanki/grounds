package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

public class MarkupTest {

  private String expected;

  @Test
  public void testBold() {
    expected = Ansi.ansi()
        .bold().a("hello").boldOff()
        .reset().toString();
    assertEquals(expected, Markup.render("%Bohello%Bx"));
  }

  @Test
  public void testItalic() {
    expected = Ansi.ansi()
        .a(Ansi.Attribute.ITALIC).a("hello").a(Ansi.Attribute.ITALIC_OFF)
        .reset().toString();
    assertEquals(expected, Markup.render("%Ithello%Ix"));
  }

  @Test
  public void testUnderline() {
    expected = Ansi.ansi()
        .a(Ansi.Attribute.UNDERLINE).a("hello ")
        .a(Ansi.Attribute.UNDERLINE_DOUBLE).a("there")
        .a(Ansi.Attribute.UNDERLINE_OFF)
        .reset().toString();
    assertEquals(expected, Markup.render("%Unhello %U2there%Ux"));
  }

  @Test
  public void testStrikethrough() {
    expected = Ansi.ansi()
        .a(Ansi.Attribute.STRIKETHROUGH_ON).a("hello").a(Ansi.Attribute.STRIKETHROUGH_OFF)
        .reset().toString();
    assertEquals(expected, Markup.render("%Sthello%Sx"));
  }

  @Test
  public void testConceal() {
    expected = Ansi.ansi()
        .a(Ansi.Attribute.CONCEAL_ON).a("hello").a(Ansi.Attribute.CONCEAL_OFF)
        .reset().toString();
    assertEquals(expected, Markup.render("%Cohello%Cx"));
  }


  // special cases

  @Test
  public void testTrailing() {
    expected = Ansi.ansi()
        .a("May the ")
        .bold().a("force").boldOff()
        .a(" be with you")
        .reset().toString();
    assertEquals(expected, Markup.render("May the %Boforce%Bx be with you"));
  }

  @Test
  public void testEscapedPercentInternal() {
    expected = Ansi.ansi()
        .a("I am 99").a("%").a(" sure")
        .toString();
    assertEquals(expected, Markup.render("I am 99%% sure"));
  }

  @Test
  public void testEscapedPercentStart() {
    expected = Ansi.ansi()
        .a("%").a(" is a percent sign")
        .toString();
    assertEquals(expected, Markup.render("%% is a percent sign"));
  }

  @Test
  public void testEscapedPercentEnd() {
    expected = Ansi.ansi()
        .a("A percent sign is ").a("%")
        .toString();
    assertEquals(expected, Markup.render("A percent sign is %%"));
  }

  @Test
  public void testNoExtraReset() {
    expected = Ansi.ansi()
        .bold().a("hello").boldOff()
        .reset().toString();
    assertEquals(expected, Markup.render("%Bohello%Bx%re"));
  }

  @Test
  public void testNoMarkup() {
    assertEquals("hello", Markup.render("hello"));
  }

  @Test
  public void testInvalidCode() {
    expected = Ansi.ansi()
        .a("hello")
        .reset().toString();
    assertEquals(expected, Markup.render("%Xohello%Xx"));
  }

  @Test
  public void testTooShortCodeAtEnd1Char() {
    expected = Ansi.ansi()
        .a("hello")
        .reset().toString();
    assertEquals(expected, Markup.render("hello%X"));
  }

  @Test
  public void testTooShortCodeAtEnd0Char() {
    expected = Ansi.ansi()
        .a("hello")
        .reset().toString();
    assertEquals(expected, Markup.render("hello%"));
  }
}
