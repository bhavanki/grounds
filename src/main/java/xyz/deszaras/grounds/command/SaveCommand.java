package xyz.deszaras.grounds.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.model.Multiverse;
import xyz.deszaras.grounds.model.Player;

public class SaveCommand extends Command {

  private final File f;

  public SaveCommand(Actor actor, Player player, File f) {
    super(actor, player);
    this.f = Objects.requireNonNull(f);
  }

  @Override
  public boolean execute() {
    try {
      Multiverse.save(f);
      actor.sendMessage("Saved multiverse to " + f.getName());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      actor.sendMessage("Failed to save multiverse: " + e.getMessage());
    }
    return false;
  }

  public static SaveCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandException {
    ensureMinArgs(commandArgs, 1);
    return new SaveCommand(actor, player, new File(commandArgs.get(0)));
  }
}
