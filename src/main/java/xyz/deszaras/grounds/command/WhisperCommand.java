package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;

/**
 * Sends a message only to another player. For non-wizards, the
 * recipient must be in the same location as the player.
 *
 * Arguments: recipient player, message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class WhisperCommand extends Command<Boolean> {

  private static final String WHISPER_FORMAT = "%s whispers: %s";

  private final Player recipient;
  private final String message;

  public WhisperCommand(Actor actor, Player player, Player recipient,
                        String message) {
    super(actor, player);
    this.recipient = Objects.requireNonNull(recipient);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  protected Boolean executeImpl() {
    String fullMessage = String.format(WHISPER_FORMAT, player.getName(), message);
    recipient.sendMessage(newMessage(Message.Style.WHISPER, fullMessage));
    return true;
  }

  public static WhisperCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Player recipient =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    String message = commandArgs.subList(1, commandArgs.size())
        .stream()
        .collect(Collectors.joining(" "));
    return new WhisperCommand(actor, player, recipient, message);
  }
}
