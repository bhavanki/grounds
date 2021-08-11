package xyz.deszaras.grounds.command.mail;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

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
 * Marks mail in a player's mailbox as read or unread.<p>
 *
 * Arguments: message index number, read flag value
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class MarkReadMailCommand extends Command<String> {

  @VisibleForTesting
  static final String NO_MESSAGES = "Your mailbox is empty.";

  @VisibleForTesting
  static final String NOT_ENOUGH_MESSAGES_FORMAT =
      "There are only %d messages in your mailbox.";

  @VisibleForTesting
  static final String MARKED_READ = "Marked as read.";

  @VisibleForTesting
  static final String MARKED_UNREAD = "Marked as unread.";

  private final int indexNumber;
  private final boolean read;

  public MarkReadMailCommand(Actor actor, Player player, int indexNumber,
                         boolean read) {
    super(actor, player);
    this.indexNumber = indexNumber;
    this.read = read;
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
    missive.setRead(read);
    return read ? MARKED_READ : MARKED_UNREAD;
  }

  public static MarkReadMailCommand newCommand(Actor actor, Player player,
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

    boolean read = true;
    if (commandArgs.size() > 1) {
      String flagValueStr = commandArgs.get(1).toUpperCase();
      switch (flagValueStr) {
        case "FALSE":
        case "F":
        case "NO":
        case "N":
        case "UNREAD":
          read = false;
          break;
        default:
          read = true;
      }
    }

    return new MarkReadMailCommand(actor, player, indexNumber, read);
  }
}
