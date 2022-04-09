package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Sends an announcement to all players with an OOC message.
 *
 * Arguments: message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
@PermittedRoles(roles = { Role.THAUMATURGE })
public class AnnounceCommand extends Command<Boolean> {

  private final String message;

  public AnnounceCommand(Actor actor, Player player, String message) {
    super(actor, player);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    for (Player recipient : Universe.getCurrent().getThings(Player.class)) {
      new PageCommand(actor, player, recipient, message).execute();
    }
    return true;
  }

  public static AnnounceCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);

    String message = commandArgs.stream()
        .collect(Collectors.joining(" "));
    return new AnnounceCommand(actor, player, message);
  }

}
