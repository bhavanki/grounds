package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.mail.DeleteMailCommand;
import xyz.deszaras.grounds.command.mail.GetMailCommand;
import xyz.deszaras.grounds.command.mail.ListMailCommand;
import xyz.deszaras.grounds.command.mail.MarkReadMailCommand;
import xyz.deszaras.grounds.command.mail.SendMailCommand;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class MailCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(MailCommand.class);

  public MailCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected Boolean executeImpl() {
    throw new UnsupportedOperationException("This is a composite command");
  }

  /**
   * Gets the mailbox for the given player, creating it if it doesn't exist
   * or cannot be found.
   *
   * @param  player player
   * @return        player's mailbox
   */
  public static Thing getMailbox(Player player) {
    try {
      Optional<Thing> mailbox = player.getMailbox();
      if (mailbox.isPresent()) {
        return mailbox.get();
      }
    } catch (MissingThingException e) {
      LOG.warn("Mailbox for {} is missing, creating new one",
               player.getName());
    }
    Thing newMailbox = Thing.build(player.getName() + " Mailbox",
                                   ImmutableList.of());
    Universe.getCurrent().addThing(newMailbox);
    newMailbox.setOwner(player);
    player.setMailbox(newMailbox);
    LOG.info("Created new mailbox for {}", player.getName());
    return newMailbox;
  }

  static final Map<String, Class<? extends Command>> MAIL_COMMANDS;

  static {
    MAIL_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("SEND", SendMailCommand.class)
        .put("LIST", ListMailCommand.class)
        .put("GET", GetMailCommand.class)
        .put("READ", GetMailCommand.class)
        .put("SHOW", GetMailCommand.class)
        .put("MARK_READ", MarkReadMailCommand.class)
        .put("DELETE", DeleteMailCommand.class)
        .put("REMOVE", DeleteMailCommand.class)
        .build();
  }

  private static final CommandFactory MAIL_COMMAND_FACTORY =
      new CommandFactory(null, MAIL_COMMANDS, null);

  public static Command newCommand(Actor actor, Player player,
                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return MAIL_COMMAND_FACTORY.getCommand(actor, player, commandArgs);
  }
}
