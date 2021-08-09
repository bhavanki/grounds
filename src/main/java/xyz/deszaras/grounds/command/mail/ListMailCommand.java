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

/**
 * Lists mail in a player's mailbox.<p>
 *
 * Arguments: none
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class ListMailCommand extends Command<String> {

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
    StringBuilder b = new StringBuilder();
    String header = "#\tSent              Sender\tSubject\n" +
        "-\t----              ------\t-------";
    b.append(AnsiUtils.color(header, Ansi.Color.CYAN, false))
        .append("\n");
    String unread = AnsiUtils.color("*", Ansi.Color.GREEN, true);
    for (int i = 1; i <= missives.size(); i++) {
      Missive missive = missives.get(i - 1);
      b.append(String.format("%d%s\t%s %s\t%s", i,
                             missive.isRead() ? "" : unread,
                             MailCommand.timestampToShortString(missive.getTimestamp()),
                             missive.getSender(),
                             missive.getSubject()));
      if (i < missives.size()) {
        b.append("\n");
      }
    }
    return b.toString();
  }

  public static ListMailCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs) {
    return new ListMailCommand(actor, player);
  }
}
