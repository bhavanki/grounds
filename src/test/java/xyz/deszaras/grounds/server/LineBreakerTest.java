package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.util.AnsiString;
import xyz.deszaras.grounds.util.AnsiUtils;

public class LineBreakerTest {

  private static final int WIDTH = 20;

  private LineBreaker lb;
  private AnsiString message;
  private AnsiString brokenMessage;

  @BeforeEach
  public void setUp() {
    lb = new LineBreaker(WIDTH);
  }

  @Test
  public void testNoBreaks() {
    message = new AnsiString("message");
    brokenMessage = new AnsiString("message");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testEmpty() {
    message = new AnsiString("");
    brokenMessage = new AnsiString("");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNormalBreak() {
    message = new AnsiString("Spread love everywhere you go");
    brokenMessage = new AnsiString("Spread love\neverywhere you go");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNormalBreakExactLength() {
    message = new AnsiString("Four score and seven years ago");
    brokenMessage = new AnsiString("Four score and seven\nyears ago");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testEndsAtExactWidth() {
    message = new AnsiString("this is just a test, this is only a test.");
    brokenMessage = new AnsiString("this is just a test,\nthis is only a test.");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testLongTokens() {
    message = new AnsiString("It's... supercalifragilisticexpialidocious! " +
        "Even though the sound of it is something quite atrocious, " +
        "If you say it loud enough you'll always sound precocious: " +
        "Supercalifragilisticexpialidocious!");
    brokenMessage = new AnsiString("It's...\n" +
        "supercalifragilisticexpialidocious!\n" +
        "Even though the\n" +
        "sound of it is\n" +
        "something quite\n" +
        "atrocious, If you\n" +
        "say it loud enough\n" +
        "you'll always sound\n" +
        "precocious:\n" +
        "Supercalifragilisticexpialidocious!");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNaturalNewlines() {
    message = new AnsiString("furu ike ya\nkawazu tobikomu\nmizu no oto");
    brokenMessage = new AnsiString("furu ike ya\nkawazu tobikomu\nmizu no oto");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNaturalNewlinesAtExactWidth() {
    message = new AnsiString("Whitecaps on the bay\nA broken signboard banging\nIn the April wind");
    brokenMessage = new AnsiString("Whitecaps on the bay\nA broken signboard\nbanging\nIn the April wind");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testFinalTrailingSpace() {
    message = new AnsiString("the final frontier ");
    brokenMessage = new AnsiString("the final frontier");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testFinalTrailingNewline() {
    message = new AnsiString("cinema\n");
    brokenMessage = new AnsiString("cinema");
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testAnsiNoBreaks() {
    message = new AnsiString(AnsiUtils.color("message", Ansi.Color.RED, false));
    brokenMessage = new AnsiString(AnsiUtils.color("message", Ansi.Color.RED, false));
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  // This test is a bit deficient. The line breaker uses AnsiString.subSequence
  // which intentionally populates a bunch of redundant escape codes and resets,
  // so even though the processed line will look the same color-wise, it will
  // have different byte content. So, this test just verifies that the content
  // without escape codes matches.
  @Test
  public void testAnsiNormalBreak() {
    message = new AnsiString(AnsiUtils.color("Spread love everywhere you go", Ansi.Color.RED, true));
    String brokenStrippedMessage = "Spread love\neverywhere you go";

    assertEquals(brokenStrippedMessage,
                 lb.insertLineBreaks(message).toStrippedString());
  }

}
