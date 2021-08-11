package xyz.deszaras.grounds.command.mail;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Optional;
import org.fusesource.jansi.Ansi;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiUtils;

/**
 * Gets mail in a player's mailbox.<p>
 *
 * Arguments: message index number
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class GetMailCommand extends Command<String> {

  @VisibleForTesting
  static final String NO_MESSAGES = "Your mailbox is empty.";

  @VisibleForTesting
  static final String NOT_ENOUGH_MESSAGES_FORMAT =
      "There are only %d messages in your mailbox.";

  private final int indexNumber;

  public GetMailCommand(Actor actor, Player player, int indexNumber) {
    super(actor, player);
    this.indexNumber = indexNumber;
  }

  @Override
  protected String executeImpl() throws CommandException {
    Mailbox mailbox = new Mailbox(MailCommand.getMailbox(player));
    if (mailbox.size() == 0) {
      return NO_MESSAGES;
    }
    if (mailbox.size() < indexNumber) {
      return String.format(NOT_ENOUGH_MESSAGES_FORMAT, mailbox.size());
    }
    Missive missive = mailbox.get(indexNumber).get();

    StringBuilder b = new StringBuilder();
    b.append(AnsiUtils.color("From:    ", Ansi.Color.CYAN, false))
        .append(missive.getSender()).append("\n");
    b.append(AnsiUtils.color("Sent:    ", Ansi.Color.CYAN, false))
        .append(MailCommand.timestampToMediumString(missive.getTimestamp()))
        .append("\n");
    b.append(AnsiUtils.color("Subject: ", Ansi.Color.CYAN, false))
        .append(missive.getSubject()).append("\n\n");
    Optional<String> body = missive.getBody();
    if (body.isEmpty()) {
      b.append("<no content>").append("\n");
    } else {
      b.append(body.get()).append("\n");
    }

    missive.setRead(true);
    return b.toString();
  }

  public static GetMailCommand newCommand(Actor actor, Player player,
                                          List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);

    int indexNumber;
    try {
      indexNumber = Integer.parseInt(commandArgs.get(0));
    } catch (NumberFormatException e) {
      throw new CommandFactoryException("Index number must be an integer");
    }
    if (indexNumber <= 0) {
      throw new CommandFactoryException("Index number must be positive");
    }

    return new GetMailCommand(actor, player, indexNumber);
  }
}
