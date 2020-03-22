package xyz.deszaras.grounds.command;

import java.util.List;
import xyz.deszaras.grounds.model.Player;

/**
 * Switches to a different player.
 *
 * Arguments: name or ID of new player
 * Checks: nothing yet, but soon
 */
public class SwitchPlayerCommand extends Command {

  private final Player newPlayer;

  public SwitchPlayerCommand(Actor actor, Player player, Player newPlayer) {
    super(actor, player);
    this.newPlayer = newPlayer;
  }

  @Override
  public boolean execute() {
    actor.setCurrentPlayer(newPlayer);
    return true;
  }

  public static SwitchPlayerCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Player newPlayer =
        ArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    return new SwitchPlayerCommand(actor, player, newPlayer);
  }
}
