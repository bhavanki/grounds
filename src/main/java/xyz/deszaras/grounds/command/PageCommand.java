package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;

/**
 * Pages a player in any location with an OOC message.
 *
 * Arguments: recipient player, message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class PageCommand extends Command<Boolean> {

  private static final String PAGE_FORMAT = "%s says OOC from afar: %s";

  private final Player recipient;
  private final String message;

  public PageCommand(Actor actor, Player player, Player recipient,
                     String message) {
    super(actor, player);
    this.recipient = Objects.requireNonNull(recipient);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    String pageMessageString = String.format(PAGE_FORMAT, player.getName(), message);
    Message pageMessage = newMessage(Message.Style.OOC, pageMessageString);

    recipient.sendMessage(pageMessage);
    return true;
  }

  public static PageCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);

    Player recipient =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0),
                                                 Player.class, player, true);

    String message = commandArgs.subList(1, commandArgs.size()).stream()
        .collect(Collectors.joining(" "));
    return new PageCommand(actor, player, recipient, message);
  }
}
