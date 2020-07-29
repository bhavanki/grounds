package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LineBreakerTest {

  private static final int WIDTH = 20;

  private LineBreaker lb;
  private String message;
  private String brokenMessage;

  @BeforeEach
  public void setUp() {
    lb = new LineBreaker(WIDTH);
  }

  @Test
  public void testNoBreaks() {
    message = "message";
    brokenMessage = "message";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testEmpty() {
    message = "";
    brokenMessage = "";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNormalBreak() {
    message = "Spread love everywhere you go";
    brokenMessage = "Spread love \neverywhere you go";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNormalBreakExactLength() {
    message = "Four score and seven years ago";
    brokenMessage = "Four score and seven\nyears ago";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testEndsAtExactWidth() {
    message = "this is just a test, this is only a test.";
    brokenMessage = "this is just a test,\nthis is only a test.";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testLongTokens() {
    message = "It's... supercalifragilisticexpialidocious! " +
        "Even though the sound of it is something quite atrocious, " +
        "If you say it loud enough you'll always sound precocious: " +
        "Supercalifragilisticexpialidocious!";
    brokenMessage = "It's... \n" +
        "supercalifragilisticexpialidocious!\n" +
        "Even though the \n" +
        "sound of it is \n" +
        "something quite \n" +
        "atrocious, If you \n" +
        "say it loud enough \n" +
        "you'll always sound \n" +
        "precocious: \n" +
        "Supercalifragilisticexpialidocious!";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNaturalNewlines() {
    message = "furu ike ya\nkawazu tobikomu\nmizu no oto";
    brokenMessage = "furu ike ya\nkawazu tobikomu\nmizu no oto";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }

  @Test
  public void testNaturalNewlinesAtExactWidth() {
    message = "Whitecaps on the bay\nA broken signboard banging\nIn the April wind";
    brokenMessage = "Whitecaps on the bay\nA broken signboard \nbanging\nIn the April wind";
    assertEquals(brokenMessage, lb.insertLineBreaks(message));
  }
}
