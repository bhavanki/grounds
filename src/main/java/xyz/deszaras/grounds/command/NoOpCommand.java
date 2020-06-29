package xyz.deszaras.grounds.command;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;

/**
 * Does nothing.
 */
@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class NoOpCommand extends Command<Boolean> {

  public NoOpCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected Boolean executeImpl() {
    return true;
  }
}
