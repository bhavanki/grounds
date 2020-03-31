package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Says a message in the player's current location. Similar to
 * posing in that what is said is received as a message by all players
 * in the same location, but also activates any listening scripts
 * from things in the location.
 *
 * Arguments: message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
public class SayCommand extends Command {

  private static final String SAY_FORMAT = "> %s says: %s";

  private final String message;

  public SayCommand(Actor actor, Player player, String message) {
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

    String sayMessage = String.format(SAY_FORMAT, player.getName(), message);

    Universe universe = location.get().getUniverse();
    location.get().getContents().stream()
        .map(id -> universe.getThing(id))
        .filter(t -> t.isPresent())
        .filter(t -> t.get() instanceof Player)
        .forEach(p -> ((Player) p.get()).sendMessage(sayMessage));

    // TBD: listeners

    return true;
  }

  public static SayCommand newCommand(Actor actor, Player player,
                                       List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String message = commandArgs.stream().collect(Collectors.joining(" "));
    return new SayCommand(actor, player, message);
  }

  public static String help() {
    return "SAY <message>\n\n" +
        "Says a message, heard by all players and things in the same location";
  }
}
