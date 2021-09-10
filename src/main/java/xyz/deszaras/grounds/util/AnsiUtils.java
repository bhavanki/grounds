package xyz.deszaras.grounds.util;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.model.Thing;

/**
 * Utilities for colors in strings.
 */
public class AnsiUtils {

  private AnsiUtils() {
  }

  /**
   * Returns the given string in the given color.
   *
   * @param  s        original string
   * @param  fgColor  color
   * @param  fgBright true for bright variant, false for regular variant
   * @return          color string
   */
  public static String color(String s, Ansi.Color fgColor, boolean fgBright) {
    return color(s, fgColor, fgBright, null, false);
  }

  /**
   * Returns the given string in the given colors.
   *
   * @param  s        original string
   * @param  fgColor  text color
   * @param  fgBright true for bright variant, false for regular variant
   * @param  bgColor  background color
   * @param  bgBright true for bright variant, false for regular variant
   * @return          color string
   */
  public static String color(String s, Ansi.Color fgColor, boolean fgBright,
                             Ansi.Color bgColor, boolean bgBright) {
    Ansi a = Ansi.ansi();
    if (fgBright) {
      a.fgBright(fgColor);
    } else {
      a.fg(fgColor);
    }
    if (bgColor != null) {
      if (bgBright) {
        a.bgBright(bgColor);
      } else {
        a.bg(bgColor);
      }
    }
    a.a(s);
    return a.reset().toString();
  }

  /**
   * Returns the (color) listing of a thing.
   *
   * @param  t      thing being listed
   * @param  showId whether to include the thing ID in the listing
   * @return   listing string
   */
  public static String listing(Thing t, boolean showId) {
    StringBuilder b = new StringBuilder(t.getName());
    if (showId) {
      b.append(" ").append(id(t));
    }
    return b.toString();
  }

  public static String id(Thing t) {
    return color("[" + t.getId() + "]", Ansi.Color.BLACK, true);
  }
}
