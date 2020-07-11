package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Forcibly moves a player to a destination anywhere in the universe.<p>
 *
 * Arguments: player to yoink, ID of destination
 */
@PermittedRoles(roles = { Role.THAUMATURGE },
                failureMessage = "You are not a thaumaturge, so you may not yoink")
public class YoinkCommand extends Command<Boolean> {

  private final Player yoinkedPlayer;
  private final Place destination;

  public YoinkCommand(Actor actor, Player player, Player yoinkedPlayer,
                      Place destination) {
    super(actor, player);
    this.yoinkedPlayer = Objects.requireNonNull(yoinkedPlayer);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  protected Boolean executeImpl() {
    Optional<Place> source;
    try {
      source = yoinkedPlayer.getLocationAsPlace();
    } catch (MissingThingException e) {
      source = Optional.empty();
    }
    if (source.isPresent()) {
      source.get().take(yoinkedPlayer);
    }

    destination.give(yoinkedPlayer);
    yoinkedPlayer.setLocation(destination);
    yoinkedPlayer.sendMessage(new Message(player, Message.Style.INFO,
                                          "You have been relocated to " +
                                          destination.getName()));

    return true;
  }

  public static YoinkCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Player yoinkedPlayer =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Player.class, player);
    Place destination =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(1), Place.class, player);
    return new YoinkCommand(actor, player, yoinkedPlayer, destination);
  }
}
