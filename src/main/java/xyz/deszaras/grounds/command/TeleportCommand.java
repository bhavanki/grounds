package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Teleports the player to a destination anywhere in the universe.<p>
 *
 * Arguments: ID of destination<br>
 * Checks: player passes GENERAL of destination
 */
public class TeleportCommand extends Command<String> {

  private final Place destination;
  private LookCommand testLookCommand;

  public TeleportCommand(Actor actor, Player player, Place destination) {
    super(actor, player);
    this.destination = Objects.requireNonNull(destination);
    testLookCommand = null;
  }

  protected void setTestLookCommand(LookCommand testLookCommand) {
    this.testLookCommand = testLookCommand;
  }

  @Override
  public String execute() throws CommandException {
    checkPermission(Category.GENERAL, destination, "You are not permitted to move there");

    Optional<Place> source;
    try {
      source = player.getLocation();
    } catch (MissingThingException e) {
      source = Optional.empty();
    }
    if (source.isPresent()) {
      source.get().take(player);
    }

    destination.give(player);
    player.setLocation(destination);

    try {
      LookCommand lookCommand =
          testLookCommand != null ? testLookCommand : new LookCommand(actor, player);
      return lookCommand.execute();
    } catch (CommandException e) {
      return e.getMessage();
    }
  }

  public static TeleportCommand newCommand(Actor actor, Player player,
                                           List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    Place destination =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Place.class, player);
    return new TeleportCommand(actor, player, destination);
  }
}
