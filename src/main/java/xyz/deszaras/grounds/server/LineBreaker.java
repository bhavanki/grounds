package xyz.deszaras.grounds.server;

import com.google.common.base.Joiner;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper class that breaks a long string into lines of a given width.
 */
public class LineBreaker {

  private final int terminalWidth;
  private final BreakIterator breakIterator;

  /**
   * Creates a new linebreaker.
   *
   * @param terminalWidth maximum line width
   */
  public LineBreaker(int terminalWidth) {
    this.terminalWidth = terminalWidth;
    this.breakIterator = BreakIterator.getLineInstance();
  }

  private static final Joiner LINE_JOINER = Joiner.on("\n");

  /**
   * Adds line breaks to a string.
   *
   * @param s string to break
   * @return string with line breaks inserted
   */
  public String insertLineBreaks(String s) {
    return LINE_JOINER.join(lineBreak(s));
  }

  /**
   * Breaks a string into lines. This is surprisingly tricky.
   *
   * @param s string to break
   * @return list of lines
   */
  public List<String> lineBreak(String s) {
    if (s.isEmpty()) {
      return Collections.singletonList(s);
    }
    List<String> lines = new ArrayList<>();
    breakIterator.setText(s);

    // Use a string builder to accumulate line content.
    StringBuilder line = new StringBuilder();
    // Iterate over substrings as returned by the iterator.
    int start = breakIterator.first();
    for (int end = breakIterator.next(); end != BreakIterator.DONE;
           start = end, end = breakIterator.next()) {
      String subs = s.substring(start, end);

      if (line.length() + (end - start) <= terminalWidth ||
          line.length() == 0) {
        // Either there is room on the line for this substring, or
        // the line is empty and the substring is longer so it'll have to
        // be put on anyway.
        line.append(subs);
      } else {
        // There is no room on the current line, so close it out and
        // begin a new line with this substring.
        lines.add(line.toString());
        line = new StringBuilder(subs);
      }

      // If the current line ends with a "natural" newline, respect it,
      // and start a new blank line.
      if (line.charAt(line.length() - 1) == '\n') {
        line.deleteCharAt(line.length() - 1);
        lines.add(line.toString());
        line = new StringBuilder();
      }
    }

    // The loop above ends with one last line under construction.
    lines.add(line.toString());

    return Collections.unmodifiableList(lines);
  }

  public static void main(String[] args) {
    int width = Integer.parseInt(args[0]);
    String s = args[1];

    for (int i = 0; i < width; i++) {
      System.out.print("X");
    }
    System.out.println("");

    for (String line : new LineBreaker(width).lineBreak(s)) {
      System.out.println(line);
    }
  }

}
