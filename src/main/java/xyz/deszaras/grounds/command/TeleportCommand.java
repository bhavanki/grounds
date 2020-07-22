package xyz.deszaras.grounds.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * Teleports the player to a destination anywhere in the universe.<p>
 *
 * Arguments: ID of destination<br>
 * Checks: player passes GENERAL of destination
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
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
  protected String executeImpl() throws CommandException {
    checkPermission(Category.GENERAL, destination, "You are not permitted to move there");

    Optional<Place> source;
    try {
      source = player.getLocationAsPlace();
    } catch (MissingThingException e) {
      source = Optional.empty();
    }
    if (source.isPresent()) {
      source.get().take(player);
    }

    destination.give(player);
    player.setLocation(destination);
    postEvent(new TeleportArrivalEvent(player, destination));

    try {
      LookCommand lookCommand =
          testLookCommand != null ? testLookCommand : new LookCommand(actor, player);
      return lookCommand.executeImpl();
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

  /**
   * The payload for TeleportArrivalEvent.
   */
  public static class TeleportArrival {
    /**
     * The name of the player arriving.
     */
    @JsonProperty
    public final String player;
    /**
     * The name of the destination.
     */
    @JsonProperty
    public final String destination;
    /**
     * The ID of the destination.
     */
    @JsonProperty
    public final String destinationId;

    private TeleportArrival(Player player, Place destination) {
      this.player = player.getName();
      this.destination = destination.getName();
      this.destinationId = destination.getId().toString();
    }
  }

  /**
   * An event posted when a player arrives at a destination.
   */
  public static class TeleportArrivalEvent extends Event<TeleportArrival> {
    private TeleportArrivalEvent(Player player, Place destination) {
      super(player, new TeleportArrival(player, destination));
    }
  }
}
