package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Sends a message only to another thing (often a player). For
 * non-wizards, the recipient must be in the same location as the
 * player.
 *
 * Arguments: recipient thing, message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
public class WhisperCommand extends Command {

  private static final String WHISPER_FORMAT = "~ %s whispers: %s";

  private final Thing recipient;
  private final String message;

  public WhisperCommand(Actor actor, Player player, Thing recipient,
                        String message) {
    super(actor, player);
    this.recipient = Objects.requireNonNull(recipient);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  public boolean execute() {
    String fullMessage = String.format(WHISPER_FORMAT, player.getName(), message);
    recipient.sendMessage(fullMessage);
    return true;
  }

  public static WhisperCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing recipient =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    String message = commandArgs.subList(1, commandArgs.size())
        .stream()
        .collect(Collectors.joining(" "));
    return new WhisperCommand(actor, player, recipient, message);
  }

  public static String help() {
    return "WHISPER <player> <message>\n\n" +
        "Sends a message only to another thing (player)";
  }
}
