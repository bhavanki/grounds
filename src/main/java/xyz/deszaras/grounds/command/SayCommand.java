package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Says a message in the player's current location. Similar to
 * posing in that what is said is received as a message by all players
 * in the same location. If the message is not OOC, it also activates
 * any listening scripts from things in the location.
 *
 * Arguments: message (quotes not necessary)
 * Checks: none at the moment, but that'll change
 */
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
  public Boolean execute() throws CommandException {
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

    // TBD: listeners (ic only)

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
}
