package xyz.deszaras.grounds.command;

import java.util.Objects;
import java.util.Set;

import xyz.deszaras.grounds.model.Player;

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
    public static final Style COMMAND_EXCEPTION = new Style("! %s");
    public static final Style SCRIPT = new Style("* %s");

    public static final Style POSE = new Style("` %s");
    public static final Style OOC = new Style("%% %s");
    public static final Style SAY = new Style("> %s");
    public static final Style WHISPER = new Style("~ %s");
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

  public String getMessage() {
    return message;
  }

  /**
   * Gets the message text, formatted according to this message's style.
   *
   * @return formatted message
   */
  public String getFormattedMessage() {
    return style.format(message);
  }
}
