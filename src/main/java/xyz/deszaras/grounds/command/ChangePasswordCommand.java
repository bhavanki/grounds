package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.util.Argon2Utils;

/**
 * Changes an actor's password. This command is meant to be run by actors
 * themselves.
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class ChangePasswordCommand extends Command<Boolean> {

  private String oldPassword;
  private String newPassword;

  public ChangePasswordCommand(Actor actor, Player player, String oldPassword,
                               String newPassword) {
    super(actor, player);
    this.oldPassword = Objects.requireNonNull(oldPassword);
    this.newPassword = Objects.requireNonNull(newPassword);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    String username = actor.getUsername();
    String currentPasswordHash =
        ActorDatabase.INSTANCE.getActorRecord(username).get().getPassword();

    if (!Argon2Utils.verifyPassword(currentPasswordHash, oldPassword)) {
      throw new CommandException("Old password does not match");
    }

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.setPassword(Argon2Utils.hashPassword(newPassword)));
    if (!result) {
      throw new CommandException("Failed to set new password");
    }
    return ActorCommand.saveActorDatabase();
  }

  public static ChangePasswordCommand newCommand(Actor actor, Player player,
                                                 List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    return new ChangePasswordCommand(actor, player, commandArgs.get(0),
                                     commandArgs.get(1));
  }
}
