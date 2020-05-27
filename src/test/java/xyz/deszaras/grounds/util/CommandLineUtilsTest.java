package xyz.deszaras.grounds.util;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class CommandLineUtilsTest {

  @Test
  public void testTokenizeBasic() {
    List<String> tokens = CommandLineUtils.tokenize("a b c");
    assertEquals(ImmutableList.of("a", "b", "c"), tokens);
  }

  @Test
  public void testTokenizeWithDoubleQuotes() {
    List<String> tokens = CommandLineUtils.tokenize("a \"b c\" d");
    assertEquals(ImmutableList.of("a", "b c", "d"), tokens);
  }

  @Test
  public void testTokenizeWithSingleQuotes() {
    List<String> tokens = CommandLineUtils.tokenize("a 'b c' d");
    assertEquals(ImmutableList.of("a", "b c", "d"), tokens);
  }

  @Test
  public void testTokenizeWithContraction() {
    List<String> tokens = CommandLineUtils.tokenize("I don't think so");
    assertEquals(ImmutableList.of("I", "don't", "think", "so"), tokens);
  }
}
