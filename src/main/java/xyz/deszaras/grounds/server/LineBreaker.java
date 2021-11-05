package xyz.deszaras.grounds.server;

import com.google.common.base.Joiner;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.deszaras.grounds.util.AnsiString;

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
  public AnsiString insertLineBreaks(AnsiString s) {
    return new AnsiString(LINE_JOINER.join(lineBreak(s)));
  }

  /**
   * Breaks a string into lines. This is surprisingly tricky.
   *
   * @param s string to break
   * @return list of lines
   */
  @SuppressWarnings("PMD.UselessParentheses")
  public List<String> lineBreak(AnsiString s) {
    if (s.isEmpty()) {
      return Collections.singletonList(s.toString());
    }
    List<String> lines = new ArrayList<>();
    breakIterator.setText(s.toStrippedString());

    // Use a string builder to accumulate line content.
    StringBuilder lineBuilder = new StringBuilder();
    AnsiString currLine = new AnsiString("");

    // Iterate over substrings as returned by the iterator.
    int start = breakIterator.first();
    for (int end = breakIterator.next(); end != BreakIterator.DONE;
           start = end, end = breakIterator.next()) {
      AnsiString subs = s.substring(start, end);

      // Handle substrings specially when they end with a space (very often) or
      // with an explicit newline.
      boolean trailingSpace = subs.charAt(subs.length() - 1) == ' ';
      boolean trailingNewline = subs.charAt(subs.length() - 1) == '\n';

      // See if the current substring shall be added to the line being built
      // now. It shall be if any of these conditions hold:
      // 1. The substring fits into the line as is.
      // 2. The substring ends with a space, and without that space fits into
      //    the line.
      // 3. The substring ends with an explicit newline, and without that
      //    newline fits into the line.
      // 4. The line is empty - in this case, the substring is longer but has to
      //    be put on anyway or we'll never progress.
      currLine = new AnsiString(lineBuilder.toString());
      int len = currLine.length();
      if (len + (end - start) <= terminalWidth ||
          (trailingSpace && len + (end - start - 1) <= terminalWidth) ||
          (trailingNewline && len + (end - start - 1) <= terminalWidth) ||
          len == 0) {
        lineBuilder.append(subs.toString());
        currLine = new AnsiString(lineBuilder.toString());
      } else {
        // There is no room on the current line, so close it out and
        // begin a new line with this substring. If the current line ends with
        // a space, trim it off.
        lines.add(trimTrailingSpace(currLine).toString());
        lineBuilder = new StringBuilder(subs.toString());
        currLine = subs;
      }
      len = currLine.length();

      // If the substring added to the line has a trailing space and that makes
      // the line length too long, chop the space off. (Condition 2)
      if (trailingSpace && len > terminalWidth) {
        currLine = trimTrailingSpace(currLine);
        lineBuilder = new StringBuilder(currLine.toString());
      }

      // If the substring added to the line ends with a "natural" newline,
      // respect it, and start a new blank line. This works even for the case
      // when the "natural" newline extends beyond the maximum line width.
      // (Condition 3)
      if (trailingNewline) {
        lines.add(currLine.substring(0, len - 1).toString());
        lineBuilder = new StringBuilder();
      }
    }

    // The loop above ends with one last line under construction. Add it if it
    // has content. If the last line ends with a space, trim it off.
    AnsiString remains = new AnsiString(lineBuilder.toString());
    if (remains.length() > 0) {
      lines.add(trimTrailingSpace(remains).toString());
    }

    return Collections.unmodifiableList(lines);
  }

  private static AnsiString trimTrailingSpace(AnsiString s) {
    if (s.length() > 0 && s.charAt(s.length() - 1) == ' ') {
      return s.substring(0, s.length() - 1);
    } else {
      return s;
    }
  }
}
