package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Emits pose text, which is received as a message by all players
 * in the same location as the player executing the command.
 *
 * Arguments: message (quotes not necessary)
 */
@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class PoseCommand extends Command<Boolean> {

  private static final String POSE_FORMAT = "%s";

  private final String message;

  public PoseCommand(Actor actor, Player player, String message) {
    super(actor, player);
    this.message = Objects.requireNonNull(message);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    Place location = getPlayerLocation("pose to anyone");

    // TBD check permission for posing in location?

    Message poseMessage = newMessage(Message.Style.POSE,
                                     String.format(POSE_FORMAT, message));

    location.getContents().stream()
        .map(id -> Universe.getCurrent().getThing(id))
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
}
