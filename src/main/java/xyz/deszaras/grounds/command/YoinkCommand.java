package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Forcibly moves a thing to a destination anywhere in the universe.<p>
 *
 * Arguments: thing to yoink, ID of destination
 */
@PermittedRoles(roles = { Role.THAUMATURGE },
                failureMessage = "You are not a thaumaturge, so you may not yoink")
public class YoinkCommand extends Command<Boolean> {

  private final Thing yoinkedThing;
  private final Place destination;

  public YoinkCommand(Actor actor, Player player, Thing yoinkedThing,
                      Place destination) {
    super(actor, player);
    this.yoinkedThing = Objects.requireNonNull(yoinkedThing);
    this.destination = Objects.requireNonNull(destination);
  }

  @Override
  protected Boolean executeImpl() {
    Optional<Thing> source;
    try {
      source = yoinkedThing.getLocation();
    } catch (MissingThingException e) {
      source = Optional.empty();
    }
    if (source.isPresent()) {
      source.get().take(yoinkedThing);
    }

    destination.give(yoinkedThing);
    yoinkedThing.setLocation(destination);
    if (yoinkedThing instanceof Player) {
      ((Player) yoinkedThing).sendMessage(new Message(player, Message.Style.INFO,
                                                      "You have been relocated to " +
                                                      destination.getName()));
    }

    return true;
  }

  public static YoinkCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing yoinkedThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    Place destination =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(1), Place.class, player);
    return new YoinkCommand(actor, player, yoinkedThing, destination);
  }
}
