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
  @SuppressWarnings("PMD.UselessParentheses")
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

      // Handle substrings specially when they end with a space (very often) or
      // with an explicit newline.
      boolean trailingSpace = subs.endsWith(" ");
      boolean trailingNewline = subs.endsWith("\n");

      // See if the current substring shall be added to the line being built
      // now. It shall be if any of these conditions hold:
      // 1. The substring fits into the line as is.
      // 2. The substring ends with a space, and without that space fits into
      //    the line.
      // 3. The substring ends with an explicit newline, and without that
      //    newline fits into the line.
      // 4. The line is empty - in this case, the substring is longer but has to
      //    be put on anyway or we'll never progress.
      if (line.length() + (end - start) <= terminalWidth ||
          (trailingSpace && line.length() + (end - start - 1) <= terminalWidth) ||
          (trailingNewline && line.length() + (end - start - 1) <= terminalWidth) ||
          line.length() == 0) {
        line.append(subs);
      } else {
        // There is no room on the current line, so close it out and
        // begin a new line with this substring.
        lines.add(line.toString());
        line = new StringBuilder(subs);
      }

      // If the substring added to the line has a trailing space and that makes
      // the line length too long, chop the space off.
      if (trailingSpace && line.length() > terminalWidth) {
        line.deleteCharAt(line.length() - 1);
      }

      // If the current line ends with a "natural" newline, respect it,
      // and start a new blank line. This works even for the case when the
      // "natural" newline extends beyond the maximum line width, i.e., when
      // trailingNewline == true and the line's length is now one longers than
      // the permitted maximum.
      if (line.charAt(line.length() - 1) == '\n') {
        line.deleteCharAt(line.length() - 1);
        lines.add(line.toString());
        line = new StringBuilder();
      }
    }

    // The loop above ends with one last line under construction. Add it if it
    // has content.
    if (line.length() > 0) {
      lines.add(line.toString());
    }

    return Collections.unmodifiableList(lines);
  }
}
