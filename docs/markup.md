# Markup

Grounds supports a basic markup language to format message text in several situations:

* The `ooc`, `page`, `pose`, `say`, and `whisper` commands
* The body of mail messages

Formatting only affects messages for players who have their `ansi` preference set to true. Otherwise, the formatting is stripped out.

## Basic Syntax

The markup language uses three-character commands to make a change to the appearance of text. Every command starts with a percent sign `%` and then has two more characters after it describing what to change. The changed formatting takes effect after the command and lasts until another command changes it, or until the end of the text.

Here are a couple of examples.

* The message "Fortune favors the %Bobold%Bx, my friend" renders the word "bold" in bold or more intense characters:

Fortune favors the **bold**, my friend

* The message "Stop, a %frred%re light" renders the word "red" in the color red:

<p>Stop, a <span style="color: #aa0000">red</span> light</p>

If you want a literal percent sign in your text, then double it (`%%`).

Any unrecognized command is ignored.

## Reset

The `%re` command resets all formatting changes that came before it, to get you back to the defaults. You don't need to give this command at the end of your message; Grounds will add it on if necessary.

## Text Attributes

Text attributes alter the shape and decoration of text. When an attribute is turned on with its "on" command, it stays in effect until it is turned off with its "off" command, or until a reset command.

Depending on each person's game client:

* Some attributes may work combined, like bold and italic.
* Some attributes might not have any effect at all.

<table>
  <tr><th>Attribute</th><th>To turn on</th><th>To turn off</th></tr>
  <tr>
    <td>Bold</td><td>%Bo</td><td>%Bx</td>
  </tr>
  <tr>
    <td>Italic</td><td>%It</td><td>%Ix</td>
  </tr>
  <tr>
    <td>Underline</td><td>%Un</td><td>%Ux</td>
  </tr>
  <tr>
    <td>Double underline</td><td>%U2</td><td>%Ux</td>
  </tr>
  <tr>
    <td>Strikethrough</td><td>%St</td><td>%Sx</td>
  </tr>
  <tr>
    <td>Conceal</td><td>%Co</td><td>%Cx</td>
  </tr>
</table>

## Color

The markup language supports the 16 basic ANSI colors for both foreground (text) and background. The general pattern for the command is either `f` or `b` for foreground or background, followed by a letter indicating the color.

You can vary foreground and background colors separately: when you change the foreground color, the background color stays the same, and vice versa.

There are no "off" commands for each color; instead, do one of the following:

* Switch to the default color using `%fd` and/or `%bd`
* Use `%re` to reset both foreground and background colors

Depending on each person's game client, the exact colors shown for each command will vary.

<table>
  <tr><th>Color</th><th>Foreground</th><th>Background</th></tr>
  <tr>
    <td>black</td><td>%fk</td><td>%bk</td>
  </tr>
  <tr>
    <td>red</td><td>%fr</td><td>%br</td>
  </tr>
  <tr>
    <td>green</td><td>%fg</td><td>%bg</td>
  </tr>
  <tr>
    <td>yellow</td><td>%fy</td><td>%by</td>
  </tr>
  <tr>
    <td>blue</td><td>%fb</td><td>%bb</td>
  </tr>
  <tr>
    <td>magenta</td><td>%fm</td><td>%bm</td>
  </tr>
  <tr>
    <td>cyan</td><td>%fc</td><td>%bc</td>
  </tr>
  <tr>
    <td>white (light gray)</td><td>%fw</td><td>%bw</td>
  </tr>
  <tr>
    <td>bright black (gray)</td><td>%fK</td><td>%bK</td>
  </tr>
  <tr>
    <td>bright red</td><td>%fR</td><td>%bR</td>
  </tr>
  <tr>
    <td>bright green</td><td>%fG</td><td>%bG</td>
  </tr>
  <tr>
    <td>bright yellow</td><td>%fY</td><td>%bY</td>
  </tr>
  <tr>
    <td>bright blue</td><td>%fB</td><td>%bB</td>
  </tr>
  <tr>
    <td>bright magenta</td><td>%fM</td><td>%bM</td>
  </tr>
  <tr>
    <td>bright cyan</td><td>%fC</td><td>%bC</td>
  </tr>
  <tr>
    <td>bright white</td><td>%fW</td><td>%bW</td>
  </tr>
  <tr>
    <td>default</td><td>%fd</td><td>%bd</td>
  </tr>
</table>
