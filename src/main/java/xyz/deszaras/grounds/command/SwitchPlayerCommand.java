package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Optional;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;

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
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    Optional<Player> newPlayer = Multiverse.MULTIVERSE.findThing(commandArgs.get(0), Player.class);
    if (!newPlayer.isPresent()) {
      throw new CommandException("Failed to find new player in universe");
    }
    return new SwitchPlayerCommand(actor, player, newPlayer.get());
  }
}
