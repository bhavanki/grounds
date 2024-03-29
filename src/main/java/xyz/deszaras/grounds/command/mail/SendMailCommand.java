package xyz.deszaras.grounds.command.mail;

import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandArgumentResolver;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.MailCommand;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.mail.Mailbox;
import xyz.deszaras.grounds.mail.Missive;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;
import xyz.deszaras.grounds.util.Markup;

/**
 * Sends mail to a player's mailbox.
 *
 * Arguments: recipients (at least one), subject, body
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class SendMailCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(SendMailCommand.class);

  private final Set<Player> recipients;
  private final String subject;
  private final String body;

  public SendMailCommand(Actor actor, Player player, Set<Player> recipients,
                         String subject, String body) {
    super(actor, player);
    this.recipients = ImmutableSet.copyOf(Objects.requireNonNull(recipients));
    this.subject = Objects.requireNonNull(subject);
    this.body = body;
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    List<String> recipientNames = recipients.stream()
        .map(Player::getName)
        .collect(Collectors.toList());
    Instant now = Instant.now();
    String renderedBody = Markup.render(body);

    for (Player recipient : recipients) {
      // TBD: Don't create if doesn't already exist?
      Mailbox mailbox = new Mailbox(MailCommand.getMailbox(recipient));

      if (recipient.mutes(player)) {
        LOG.info("Mail from player {} to recipient {} is muted",
                 player.getName(), recipient.getName());
        // sender does not see effect of muting
        continue;
      }

      // Each player gets their own copy (thing) of the mail.
      Missive missive = new Missive(player.getName(), subject, recipientNames,
                                    now, renderedBody);
      Universe.getCurrent().addThing(missive.getThing());
      missive.getThing().setOwner(recipient);
      mailbox.deliver(missive);
      recipient.sendMessage(newInfoMessage("You have new mail."));
    }

    player.sendMessage(newInfoMessage("Sent to " + recipients.size() +
                                      " recipients"));
    return true;
  }

  public static SendMailCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 3);

    // TBD: maximum recipient count?
    Set<Player> recipients = new HashSet<>();
    for (int i = 0; i < commandArgs.size() - 2; i++) {
      Player recipientPlayer =
          CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(i),
                                                   Player.class, player, true);
      recipients.add(recipientPlayer);
    }

    String subject = commandArgs.get(commandArgs.size() - 2);
    if (subject.length() == 0) {
      throw new CommandFactoryException("Subject may not be empty");
    }
    String body = commandArgs.get(commandArgs.size() - 1);

    return new SendMailCommand(actor, player, recipients, subject, body);
  }
}
