package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;

@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class ExitCommand extends Command<Boolean> {

  public ExitCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected Boolean executeImpl() {
    return true;
  }

  public static ExitCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs) {
    return new ExitCommand(actor, player);
  }

}
