package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

public class AnsiStringTest {

  private AnsiString s;

  @Test
  public void testLengthAnsi() {
    String original = AnsiUtils.color("length", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    assertEquals(6, s.length());
  }

  @Test
  public void testLengthNoAnsi() {
    String original = "length";
    s = new AnsiString(original);

    assertEquals(6, s.length());
  }

  private static final String EMPTY_CYAN =
      AnsiUtils.color("", Ansi.Color.CYAN, false);

  @Test
  public void testIsEmptyAnsi() {
    String original = EMPTY_CYAN;
    s = new AnsiString(original);

    assertTrue(s.isEmpty());
  }

  @Test
  public void testIsEmptyNoAnsi() {
    String original = "";
    s = new AnsiString(original);

    assertTrue(s.isEmpty());
  }

  @Test
  public void testSubSequenceAnsi() {
    String original = AnsiUtils.color("subSequence", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    String expected = AnsiUtils.color("subS", Ansi.Color.CYAN, false);
    assertEquals(expected, s.subSequence(0, 4).toString());
    expected = AnsiUtils.color("ence", Ansi.Color.CYAN, false);
    assertEquals(expected, s.subSequence(7, 11).toString());
    expected = AnsiUtils.color("Sequ", Ansi.Color.CYAN, false);
    assertEquals(expected, s.subSequence(3, 7).toString());
    expected = EMPTY_CYAN;
    assertEquals(expected, s.subSequence(6, 6).toString());
  }

  @Test
  public void testSubSequenceAnsi2() {
    String original = AnsiUtils.color("subSequence1", Ansi.Color.CYAN, false) +
        " plain1 " + AnsiUtils.color("subSequence2", Ansi.Color.MAGENTA, false);
    s = new AnsiString(original);

    String expected = AnsiUtils.color("subS", Ansi.Color.CYAN, false);
    assertEquals(expected, s.subSequence(0, 4).toString());
    expected = EMPTY_CYAN + AnsiUtils.color("nce2", Ansi.Color.MAGENTA, false);
    assertEquals(expected, s.subSequence(28, 32).toString());
    expected = EMPTY_CYAN + " pla" + Ansi.ansi().reset().toString();
    assertEquals(expected, s.subSequence(12, 16).toString());
  }

  @Test
  public void testSubSequenceNoAnsi() {
    String original = "subSequence";
    s = new AnsiString(original);

    assertEquals("subS", s.subSequence(0, 4).toString());
    assertEquals("ence", s.subSequence(7, 11).toString());
    assertEquals("Sequ", s.subSequence(3, 7).toString());
    assertEquals("", s.subSequence(6, 6).toString());
  }

  @Test
  public void testNormalAnsi() {
    String original = AnsiUtils.color("normal", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    String fs = String.format("%s", s);

    assertEquals(original, fs);
  }

  @Test
  public void testNormalNoAnsi() {
    s = new AnsiString("normal");

    String fs = String.format("%s", s);

    assertEquals("normal", fs);
  }

  @Test
  public void testPrecisionAnsi() {
    String original = AnsiUtils.color("precision", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    String fs = String.format("%.6s", s);

    String expected = AnsiUtils.color("precis", Ansi.Color.CYAN, false);
    assertEquals(expected, fs);
  }

  @Test
  public void testPrecisionNoAnsi() {
    s = new AnsiString("precison");

    String fs = String.format("%.6s", s);

    assertEquals("precis", fs);
  }

  @Test
  public void testWidthRightJustifyAnsi() {
    String original = AnsiUtils.color("justify", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    String fs = String.format("%10s", s);

    assertEquals("   " + original, fs);
  }

  @Test
  public void testWidthRightJustifyNoAnsi() {
    s = new AnsiString("justify");

    String fs = String.format("%10s", s);

    assertEquals("   justify", fs);
  }

  @Test
  public void testWidthLeftJustifyAnsi() {
    String original = AnsiUtils.color("justify", Ansi.Color.CYAN, false);
    s = new AnsiString(original);

    String fs = String.format("%-10s", s);

    assertEquals(original + "   ", fs);
  }

  @Test
  public void testWidthLeftJustifyNoAnsi() {
    s = new AnsiString("justify");

    String fs = String.format("%-10s", s);

    assertEquals("justify   ", fs);
  }

  // @Test
  // public void testUppercaseAnsi() {
  //   String original = AnsiUtils.color("uppercase", Ansi.Color.CYAN, false);
  //   s = new AnsiString(original);

  //   String fs = String.format("%S", s);

  //   String expected = AnsiUtils.color("UPPERCASE", Ansi.Color.CYAN, false);
  //   assertEquals(expected, fs);
  // }

  // @Test
  // public void testUppercaseNoAnsi() {
  //   s = new AnsiString("uppercase");

  //   String fs = String.format("%S", s);

  //   assertEquals("UPPERCASE", fs);
  // }
}
