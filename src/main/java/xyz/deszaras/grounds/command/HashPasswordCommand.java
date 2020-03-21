package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.HashedPasswordAuthenticator;

/**
 * A command to generate the hash for a password. The hash would be entered
 * into the password file for a user.
 */
public class HashPasswordCommand extends Command {

  private final String password;

  /**
   * Creates a new command.
   *
   * @param actor actor
   * @param player player
   * @param password password to hash
   */
  public HashPasswordCommand(Actor actor, Player player, String password) {
    super(actor, player);

    this.password = password;
  }

  @Override
  public boolean execute() {
    actor.sendMessage(HashedPasswordAuthenticator.hashPassword(password));
    return true;
  }

  public static HashPasswordCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    return new HashPasswordCommand(actor, player, commandArgs.get(0));
  }
}
