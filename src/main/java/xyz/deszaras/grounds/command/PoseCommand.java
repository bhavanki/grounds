package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Emits pose text, which is received as a message by all players
 * in the same location as the player executing the command.
 *
 * Arguments: message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
public class PoseCommand extends Command {

  private static final String POSE_FORMAT = "` %s";

  private final String message;

  public PoseCommand(Actor actor, Player player, String message) {
    super(actor, player);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  public boolean execute() {
    Optional<Place> location = player.getLocation();
    if (location.isEmpty()) {
      actor.sendMessage("You aren't located anywhere, so there is no one to pose to");
      return false;
    }

    // TBD check permission for posing in location?

    String poseMessage = String.format(POSE_FORMAT, message);

    Universe universe = location.get().getUniverse();
    location.get().getContents().stream()
        .map(id -> universe.getThing(id))
        .filter(t -> t.isPresent())
        .filter(t -> t.get() instanceof Player)
        .forEach(p -> ((Player) p.get()).sendMessage(poseMessage));
    return true;
  }

  public static PoseCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String message = commandArgs.stream().collect(Collectors.joining(" "));
    return new PoseCommand(actor, player, message);
  }

  public static String help() {
    return "POSE <message>\n\n" +
        "Emits a message to all players in the same location";
  }
}
