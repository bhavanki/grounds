package xyz.deszaras.grounds.util;

import org.fusesource.jansi.Ansi;

/**
 * A very lightweight markup scheme for message text supporting the 16 basic
 * ANSI colors and several attributes (bold, italic, etc.).
 */
public final class Markup {

  private static final char FORMAT_CHAR = '%';

  private static final String CMD_RESET = "re";
  private static final String CMD_INVALID = "xx";

  private Markup() {
  }

  /**
   * Renders the given markup string.
   *
   * @param  s string to render
   * @return rendered string, or original if it lacks any markup
   */
  public static String render(String s) {
    Ansi a = Ansi.ansi();

    int fromIndex = 0;
    boolean needReset = false;
    for (int fcidx = s.indexOf(FORMAT_CHAR, fromIndex);
         fcidx != -1;
         fcidx = s.indexOf(FORMAT_CHAR, fromIndex)) {
      if (fcidx - fromIndex > 1) {
        a.a(s, fromIndex, fcidx);
      }

      // Look for escaped format character
      if (fcidx <= s.length() - 2 && s.charAt(fcidx + 1) == FORMAT_CHAR) {
        a.a(FORMAT_CHAR);
        fromIndex = fcidx + 2;
        continue;
      }

      needReset = true;
      String cmd;
      if (fcidx >= s.length() - 2) {
        // Not enough room for a command, so drop it
        cmd = CMD_INVALID;
      } else {
        cmd = s.substring(fcidx + 1, fcidx + 3);
      }

      switch (cmd) {
      case "Bo":
        a.bold();
        break;
      case "Bx":
        a.boldOff();
        break;
      case "It":
        a.a(Ansi.Attribute.ITALIC);
        break;
      case "Ix":
        a.a(Ansi.Attribute.ITALIC_OFF);
        break;
      case "Un":
        a.a(Ansi.Attribute.UNDERLINE);
        break;
      case "U2":
        a.a(Ansi.Attribute.UNDERLINE_DOUBLE);
        break;
      case "Ux":
        a.a(Ansi.Attribute.UNDERLINE_OFF);
        break;
      case "St":
        a.a(Ansi.Attribute.STRIKETHROUGH_ON);
        break;
      case "Sx":
        a.a(Ansi.Attribute.STRIKETHROUGH_OFF);
        break;
      case "Co":
        a.a(Ansi.Attribute.CONCEAL_ON);
        break;
      case "Cx":
        a.a(Ansi.Attribute.CONCEAL_OFF);
        break;
      case "fk":
        a.fgBlack();
        break;
      case "fK":
        a.fgBrightBlack();
        break;
      case "fb":
        a.fgBlue();
        break;
      case "fB":
        a.fgBrightBlue();
        break;
      case "fc":
        a.fgCyan();
        break;
      case "fC":
        a.fgBrightCyan();
        break;
      case "fg":
        a.fgGreen();
        break;
      case "fG":
        a.fgBrightGreen();
        break;
      case "fm":
        a.fgMagenta();
        break;
      case "fM":
        a.fgBrightMagenta();
        break;
      case "fr":
        a.fgRed();
        break;
      case "fR":
        a.fgBrightRed();
        break;
      case "fy":
        a.fgYellow();
        break;
      case "fY":
        a.fgBrightYellow();
        break;
      case "fw":
        a.fg(Ansi.Color.WHITE);
        break;
      case "fW":
        a.fgBright(Ansi.Color.WHITE);
        break;
      case "fd":
        a.fgDefault();
        break;
      case "bk":
        a.bg(Ansi.Color.BLACK);
        break;
      case "bK":
        a.bgBright(Ansi.Color.BLACK);
        break;
      case "bb":
        a.bg(Ansi.Color.BLUE);
        break;
      case "bB":
        a.bgBright(Ansi.Color.BLUE);
        break;
      case "bc":
        a.bgCyan();
        break;
      case "bC":
        a.bgBrightCyan();
        break;
      case "bg":
        a.bgGreen();
        break;
      case "bG":
        a.bgBrightGreen();
        break;
      case "bm":
        a.bgMagenta();
        break;
      case "bM":
        a.bgBrightMagenta();
        break;
      case "br":
        a.bgRed();
        break;
      case "bR":
        a.bgBrightRed();
        break;
      case "by":
        a.bgYellow();
        break;
      case "bY":
        a.bgBrightYellow();
        break;
      case "bw":
        a.bg(Ansi.Color.WHITE);
        break;
      case "bW":
        a.bgBright(Ansi.Color.WHITE);
        break;
      case "bd":
        a.bgDefault();
        break;
      case CMD_RESET:
        a.reset();
        needReset = false;
        break;
      case CMD_INVALID:
        // fallthrough
      default:
        // drop it
      }
      fromIndex = fcidx + 3;
    }
    if (fromIndex == 0) {
      return s;
    }
    if (fromIndex < s.length()) {
      a.a(s.substring(fromIndex));
    }
    if (needReset) {
      a.reset();
    }
    return a.toString();
  }

  public static void main(String[] args) {
    System.out.println(Markup.render(args[0]));
  }
}
