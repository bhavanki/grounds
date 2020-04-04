package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;
import xyz.deszaras.grounds.model.Link;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Gets the ID of a thing by its name and type in the player's universe. If
 * there are multiple matches, an arbitrary one is returned.
 */
public class GetIdCommand extends Command {

  private final String name;
  private final Class<? extends Thing> type;

  public GetIdCommand(Actor actor, Player player, String name, Class<? extends Thing> type) {
    super(actor, player);
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public boolean execute() {
    Optional<? extends Thing> thing = player.getUniverse().getThingByName(name, type);
    if (!thing.isEmpty()) {
      actor.sendMessage(thing.get().getId().toString());
      return true;
    }
    return false;
  }

  private static final Map<String, Class<? extends Thing>> TYPES =
      ImmutableMap.<String, Class<? extends Thing>>builder()
      .put("THING", Thing.class)
      .put("PLAYER", Player.class)
      .put("PLACE", Place.class)
      .put("LINK", Link.class)
      .put("EXTENSION", Extension.class)
      .build();

  public static GetIdCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Class<? extends Thing> type = TYPES.get(commandArgs.get(1).toUpperCase());
    if (type == null) {
      throw new CommandFactoryException("Unsupported type " + commandArgs.get(1));
    }
    return new GetIdCommand(actor, player, commandArgs.get(0), type);
  }

  public static String help() {
    return "GET_ID <name> <type>\n\n" +
        "Gets the ID of a thing in the player's universe.\n\n" +
        "Supported types: thing, player, place, link, extension";
  }
}