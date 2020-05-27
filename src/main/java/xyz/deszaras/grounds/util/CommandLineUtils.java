package xyz.deszaras.grounds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandLineUtils {

  // https://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
  // This doesn't obey escaped quotes, though.
  private static final Pattern TOKENIZE_PATTERN =
      Pattern.compile("[^\\s\"'][^\\s]*|\"([^\"]*)\"|'([^']*)'");

  /**
   * Splits a line of text into tokens. Generally, tokens are separated
   * by whitespace, but text surrounded by single or double quotes
   * is kept together as a single token (without the quotes).
   *
   * @param line line of text
   * @return tokens in line
   */
  public static List<String> tokenize(String line) {
    List<String> tokens = new ArrayList<>();
    Matcher m = TOKENIZE_PATTERN.matcher(line);
    while (m.find()) {
      if (m.group(1) != null) {
        // quotation marks
        tokens.add(m.group(1));
      } else if (m.group(2) != null) {
        // apostrophes
        tokens.add(m.group(2));
      } else {
        tokens.add(m.group());
      }
    }
    return tokens;
  }


}
