package xyz.deszaras.grounds.command.mail;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

import java.util.List;
import java.util.Optional;
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
import xyz.deszaras.grounds.util.RecordOutput;
import xyz.deszaras.grounds.util.TimeUtils;

/**
 * Gets mail in a player's mailbox.<p>
 *
 * Arguments: message index number
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class GetMailCommand extends Command<String> {

  private static final Joiner RECIPIENTS_JOINER = Joiner.on(", ");

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

    RecordOutput recordOutput = new RecordOutput()
        .addField("From", missive.getSender())
        .addField("To", RECIPIENTS_JOINER.join(missive.getRecipients()))
        .addField("Sent", TimeUtils.toString(missive.getTimestamp(), actor.getTimezone()))
        .addField("Subject", missive.getSubject())
        .addBlankLine();

    Optional<String> body = missive.getBody();
    if (body.isEmpty()) {
      recordOutput.addValue("<no content>");
    } else {
      recordOutput.addValue(body.get());
    }

    missive.setRead(true);
    return recordOutput.toString();
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
