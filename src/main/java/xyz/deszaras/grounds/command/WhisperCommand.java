package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Player;

/**
 * Sends a message only to another player. For non-wizards, the
 * recipient player must be in the same location as the player.
 *
 * Arguments: recipient player, message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
public class WhisperCommand extends Command {

  private static final String PREFIX = "Whisper from %s: ";

  private final Player recipient;
  private final String message;

  public WhisperCommand(Actor actor, Player player, Player recipient,
                        String message) {
    super(actor, player);
    this.recipient = Objects.requireNonNull(recipient);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  public boolean execute() {
    Optional<Actor> recipientActor = recipient.getCurrentActor();
    if (recipientActor.isEmpty()) {
      actor.sendMessage(player.getName() + " is idle");
      return false;
    }
    String fullMessage = String.format(PREFIX + message, player.getName());
    recipientActor.get().sendMessage(fullMessage);
    return true;
  }

  public static WhisperCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Player recipientPlayer =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    String message = commandArgs.subList(1, commandArgs.size())
        .stream()
        .collect(Collectors.joining(" "));
    return new WhisperCommand(actor, player, recipientPlayer, message);
  }

  public static String help() {
    return "WHISPER <player> <message>\n\n" +
        "Sends a message only to a player";
  }
}
