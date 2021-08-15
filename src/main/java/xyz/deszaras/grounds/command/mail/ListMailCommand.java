package xyz.deszaras.grounds.command.mail;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import org.fusesource.jansi.Ansi;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.util.AnsiUtils;
import xyz.deszaras.grounds.util.TabularOutput;
import xyz.deszaras.grounds.util.TimeUtils;

/**
 * Lists mail in a player's mailbox.<p>
 *
 * Arguments: none
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class ListMailCommand extends Command<String> {

  private static final String UNREAD =
      AnsiUtils.color("*", Ansi.Color.GREEN, true);
  private static final String SENT_COLUMN_FORMAT =
      "%-" + TimeUtils.getInstantShortStringLength() + "." +
      TimeUtils.getInstantShortStringLength() + "s";
  @VisibleForTesting
  static final String NO_MESSAGES = "Your mailbox is empty.";

  public ListMailCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    Mailbox mailbox = new Mailbox(MailCommand.getMailbox(player));
    List<Missive> missives = mailbox.getAllInReverseChronoOrder();
    if (missives.isEmpty()) {
      return NO_MESSAGES;
    }

    TabularOutput table = new TabularOutput()
        .defineColumn("R", "%1s")
        .defineColumn("#", "%3s")
        .defineColumn("SENT", SENT_COLUMN_FORMAT)
        .defineColumn("FROM", "%-20.20s")
        .defineColumn("SUBJECT", "%-30.30s");

    for (int i = 1; i <= missives.size(); i++) {
      Missive missive = missives.get(i - 1);
      table.addRow(missive.isRead() ? " " : UNREAD,
                   Integer.toString(i),
                   TimeUtils.toShortString(missive.getTimestamp()),
                   missive.getSender(),
                   missive.getSubject());
    }

    return table.toString();
  }

  public static ListMailCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs) {
    return new ListMailCommand(actor, player);
  }
}
