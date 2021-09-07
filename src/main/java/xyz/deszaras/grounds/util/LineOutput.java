package xyz.deszaras.grounds.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import org.fusesource.jansi.Ansi;

/**
 * A helper class for producing a horizontal line string.
 */
public class LineOutput {

  private final int width;
  private final String characters;
  private final Ansi.Color color;
  private final boolean bright;

  public LineOutput(int width) {
    this(width, "-", null, false);
  }

  public LineOutput(int width, String characters) {
    this(width, characters, null, false);
  }

  public LineOutput(int width, String characters, Ansi.Color color,
                    boolean bright) {
    checkArgument(width >= 0, "width must be non-negative");
    this.width = width;
    this.characters = Objects.requireNonNull(characters);
    checkArgument(characters.length() > 0, "characters must not be empty");
    checkArgument(characters.length() <= width,
                  "characters must not exceed width " + width);
    this.color = color;
    this.bright = bright;
  }

  @Override
  public String toString() {
    String line = characters.repeat(width / characters.length());
    if (color != null) {
      return AnsiUtils.color(line, color, bright);
    } else {
      return line;
    }
  }
}
