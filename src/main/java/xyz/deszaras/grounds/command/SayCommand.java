package xyz.deszaras.grounds.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Says a message in the player's current location. Similar to
 * posing in that what is said is received as a message by all players
 * in the same location. If the message is not OOC, it also produces
 * an event to be handled by listener attributes.
 *
 * Arguments: message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
@PermittedRoles(roles = { Role.GUEST, Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class SayCommand extends Command<Boolean> {

  private static final String SAY_FORMAT = "%s says: %s";
  private static final String OOC_FORMAT = "%s says OOC: %s";

  private final String message;
  private final boolean ooc;

  public SayCommand(Actor actor, Player player, String message, boolean ooc) {
    super(actor, player);
    this.message = Objects.requireNonNull(message);
    this.ooc = ooc;
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    Place location = getPlayerLocation("say anything to anyone");

    // TBD check permission for posing in location?

    String sayMessageString;
    Message sayMessage;
    if (ooc) {
      sayMessageString = String.format(OOC_FORMAT, player.getName(), message);
      sayMessage = newMessage(Message.Style.OOC, sayMessageString);
    } else {
      sayMessageString = String.format(SAY_FORMAT, player.getName(), message);
      sayMessage = newMessage(Message.Style.SAY, sayMessageString);
    }

    location.getContents().stream()
        .map(id -> Universe.getCurrent().getThing(id))
        .filter(t -> t.isPresent())
        .filter(t -> t.get() instanceof Player)
        .forEach(p -> ((Player) p.get()).sendMessage(sayMessage));

    if (!ooc) {
      postEvent(new SayMessageEvent(player, location, message));
    }

    return true;
  }

  public static SayCommand newCommand(Actor actor, Player player,
                                      List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    boolean ooc = commandArgs.get(0).equals("_ooc_");
    String message;
    if (ooc) {
      ensureMinArgs(commandArgs, 2);
      message = commandArgs.subList(1, commandArgs.size()).stream()
          .collect(Collectors.joining(" "));
    } else {
      message = commandArgs.stream().collect(Collectors.joining(" "));
    }
    return new SayCommand(actor, player, message, ooc);
  }
  /**
   * The payload for {@link SayMessageEvent}.
   */
  public static class SayMessage {
    /**
     * What was said.
     */
    @JsonProperty
    public final String message;

    SayMessage(String message) {
      this.message = message;
    }
  }

  /**
   * An event posted when a player says something.
   */
  public static class SayMessageEvent extends Event<SayMessage> {
    SayMessageEvent(Player player, Place location, String message) {
      super(player, location, new SayMessage(message));
    }
  }
}
