package xyz.deszaras.grounds.util;

import static java.util.FormattableFlags.LEFT_JUSTIFY;
// import static java.util.FormattableFlags.UPPERCASE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.stream.IntStream;

import org.fusesource.jansi.AnsiOutputStream;

/**
 * A expanded re-implementation of JAnsi's {@code AnsiString}, which disappeared
 * in the 2.0 version. Like the original, this one reports the string length
 * "correctly", with ANSI escape codes having zero length even though they are
 * characters present in the sequence. Also:<p>
 *
 * <ul>
 * <li>This class implements {@code Formattable} so that it can be used in
 *     format strings correctly via the {@code %s} specifier. Width,
 *     justification and precision are supported.</li>
 * <li>Streams from the character sequence omit escape codes.
 *     {@link #charAt(int)} ignores them.</li>
 * <li>{@link #subSequence(int, int)} and {@link #substring(int, int)} ignore
 *     escape codes in their length calculations, but preserve them in the
 *     returned subsequence (substring), with the following caveats:<ul>
 *     <li>All escape codes are copied over into the subsequence, even those
 *         that are before start. This ensures that the initial state of the
 *         subsequence matches the original, even if some codes have already
 *         been mooted.</li>
 *     <li>The subsequence always ends with an SGR reset, so that the string's
 *         trailing state won't go on to alter strings output later.</li>
 *     </ul></li>
 * </ul>
 */
public class AnsiString implements CharSequence, Formattable {

  private static final char ESC = (char) 27;

  private final CharSequence original;
  private final String stripped;

  /**
   * Creates a new string.
   *
   * @param  str string / character sequence to wrap
   */
  public AnsiString(CharSequence original) {
    this.original = Objects.requireNonNull(original);
    stripped = strip(original);
  }

  private static String strip(CharSequence original) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // AnsiOutputStream in JAnsi 2.x is much different ...
    AnsiOutputStream aos = new AnsiOutputStream(baos);

    try {
      aos.write(original.toString().getBytes());
      aos.flush();
      aos.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to strip ANSI escape codes", e);
    }

    return new String(baos.toByteArray());
  }

  @Override
  public char charAt(int index) {
    return stripped.charAt(index);
  }

  @Override
  public IntStream chars() {
    return stripped.chars();
  }

  @Override
  public IntStream codePoints() {
    return stripped.codePoints();
  }

  /**
   * Returns true if, and only if, {@link #length()} is 0.
   *
   * @return true if {@link #length()} is 0, otherwise false
   */
  public boolean isEmpty() {
    return stripped.length() == 0;
  }

  @Override
  public int length() {
    return stripped.length();
  }

  private static final char[] RESET = {ESC, '[', 'm'}; // 0 is skipped

  // This uses a covariant return type.
  @Override
  public AnsiString subSequence(int start, int end) {
    if (start < 0) {
      throw new IndexOutOfBoundsException("start must be non-negative");
    }
    if (end < 0) {
      throw new IndexOutOfBoundsException("end must be non-negative");
    }
    if (end > length()) {
      throw new IndexOutOfBoundsException("end " + end + " must not exceed " + length());
    }
    if (start > end) {
      throw new IndexOutOfBoundsException("start " + start + " must not exceed end " + end);
    }

    StringBuilder chars = new StringBuilder();
    int oidx = 0;  // index into original string including escape codes
    int idx = 0;   // index into original string not including escape codes
    boolean csiCodesSeen = false;
    while (idx < end) {
      char c = original.charAt(oidx++);
      switch (c) {
        case ESC:
          consume(chars, c, idx, start, true);
          char c2 = original.charAt(oidx++);
          consume(chars, c2, idx, start, true);
          char c3;
          switch (c2) {
            case '[': // CSI
              csiCodesSeen = true;
              do {
                c3 = original.charAt(oidx++);
                consume(chars, c3, idx, start, true);
              } while (c3 < '@' || c3 > '~');
              break;
            case ']': // OSC
              do {
                c3 = original.charAt(oidx++);
                consume(chars, c3, idx, start, true);
                if (c3 == ESC) {
                  c3 = original.charAt(oidx++);
                  consume(chars, c3, idx, start, true);
                  if (c3 == '\\') { // ST
                    break;
                  }
                  c3 = 'a';  // hack to avoid leaving the do-while loop
                }
              } while (c3 != 7);
              break;
            case '(': // CHARSET 0 - in JAnsi, character set selection
            case ')': // CHARSET 1 - ditto
              // https://github.com/fusesource/jansi/commit/ef2d858448215ef8639663c186653c501ddfb932
              c3 = original.charAt(oidx++);
              consume(chars, c3, idx, start, true);
              break;
            default: // unsupported
              // just keep going and hope for the best
          }
          break;
        default:
          consume(chars, c, idx, start, false);
          idx++;
      }
    }

    if (csiCodesSeen) {
      chars.append(RESET);
    }

    return new AnsiString(chars.toString());
  }

  /**
   * Returns a string that is a substring of this string. Alias for
   * {@link #subSequence(int, int)}.
   *
   * @param  start the beginning index, inclusive
   * @param  end   the ending index, exclusive
   * @return       the specified substring
   */
  public AnsiString substring(int start, int end) {
    return subSequence(start, end);
  }

  private static void consume(StringBuilder b, char c, int idx, int start, boolean ansiCode) {
    if (idx >= start || ansiCode) {
      b.append(c);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    return this.original.equals(((AnsiString) o).original);
  }

  @Override
  public int hashCode() {
    return Objects.hash(original);
  }

  @Override
  public String toString() {
    return original.toString();
  }

  /**
   * Returns this string stripped of any ANSI codes.
   *
   * @return string with ANSI codes stripped out
   */
  public String toStrippedString() {
    return stripped;
  }

  @Override
  public void formatTo(Formatter formatter, int flags, int width, int precision) {
    StringBuilder b = new StringBuilder();

    // Apply precision
    if (precision == -1 || length() < precision) {
      b.append(toString());
    } else {
      b.append(subSequence(0, precision).toString());
    }

    // Apply width and justification
    int len = length();
    if (len < width) {
      for (int i = 0; i < width - len; i++) {
        if ((flags & LEFT_JUSTIFY) == LEFT_JUSTIFY) {
          b.append(' ');
        } else {
          b.insert(0, ' ');
        }
      }
    }

    // Apply uppercasing
    // FIXME: uppercases letters in escape codes too
    // String s = b.toString();
    // if ((flags & UPPERCASE) == UPPERCASE) {
    //   s = s.toUpperCase(formatter.locale());
    // }

    formatter.format(b.toString());
  }
}
