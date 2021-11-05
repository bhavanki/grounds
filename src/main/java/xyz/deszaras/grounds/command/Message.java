package xyz.deszaras.grounds.command;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fusesource.jansi.Ansi;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiString;
import xyz.deszaras.grounds.util.AnsiUtils;

/**
 * A message sent to an actor.
 */
public class Message {

  /**
   * A message style, which dictates how message text is formatted.
   */
  public static class Style {
    private final String format;

    /**
     * Creates a new style.
     *
     * @param  format format string to apply to message test
     */
    public Style(String format) {
      this.format = Objects.requireNonNull(format);
    }

    /**
     * Gets this style's format string.
     *
     * @return format string
     */
    public String getFormat() {
      return format;
    }

    /**
     * Formats message text.
     * @param  s string containing message text
     * @return   formatted message text
     */
    public String format(String s) {
      return String.format(format, s != null ? s : "?");
    }

    public static final Style INFO = new Style("%s");
    public static final Style COMMAND_EXCEPTION =
        new Style(AnsiUtils.color("! %s", Ansi.Color.RED, true));
    public static final Style COMMAND_FACTORY_EXCEPTION =
        new Style(AnsiUtils.color("! %s", Ansi.Color.RED, true));
    public static final Style EXECUTION_EXCEPTION =
        new Style(AnsiUtils.color("! %s", Ansi.Color.RED, true));

    public static final Style SCRIPT = new Style("* %s");
    public static final Style COMBAT =
        new Style(AnsiUtils.color("+ %s", Ansi.Color.MAGENTA, true));

    public static final Style POSE =
        new Style(AnsiUtils.color(": %s", Ansi.Color.WHITE, true));
    public static final Style OOC =
        new Style(AnsiUtils.color("%% %s", Ansi.Color.YELLOW, false));
    public static final Style SAY =
        new Style(AnsiUtils.color("> %s", Ansi.Color.WHITE, true));
    public static final Style WHISPER =
        new Style(AnsiUtils.color("~ %s", Ansi.Color.BLACK, true));
  }

  public static final Set<Style> SYSTEM_STYLES =
      Set.of(Style.INFO, Style.POSE, Style.OOC, Style.SAY, Style.WHISPER);

  public boolean matchesSystemStyle(Style s) {
    return SYSTEM_STYLES.stream()
        .anyMatch(ss -> ss.getFormat().trim().equals(s.getFormat().trim()));
  }

  private final Player sender;
  private final Style style;
  private final String message;

  /**
   * Creates a new message.
   *
   * @param  sender  sending player
   * @param  style   message style
   * @param  message message text
   * @throws NullPointerException if any argument is null
   */
  public Message(Player sender, Style style, String message) {
    this.sender = Objects.requireNonNull(sender);
    this.style = Objects.requireNonNull(style);
    this.message = Objects.requireNonNull(message);
  }

  public Player getSender() {
    return sender;
  }

  public Style getStyle() {
    return style;
  }

  public String getMessage() {
    return message;
  }

  public AnsiString getStyledMessage() {
    return new AnsiString(style.format(message));
  }

  private static final Pattern HR_PATTERN = Pattern.compile("\\{hr ([^ }]+)( .*)?\\}");

  public Message expandHorizontalRules(int terminalWidth) {
    String msg = message;

    // {hr <element> <optional title>}
    for (Matcher matcher = HR_PATTERN.matcher(msg);
         matcher.find();
         matcher = HR_PATTERN.matcher(msg)) {
      String element = matcher.group(1);
      String rule = element.repeat(terminalWidth / element.length());

      String title = matcher.group(2);
      if (title != null && title.length() > 0) {
        String ruleStart = title.trim() + " ";
        rule = new StringBuilder(rule)
            .replace(0, ruleStart.length(), ruleStart)
            .toString();
        if (rule.length() > terminalWidth) {
          rule = rule.substring(0, terminalWidth);
        }
      }
      msg = msg.substring(0, matcher.start()) + rule + msg.substring(matcher.end());
    }

    return new Message(sender, style, msg);
  }
}
