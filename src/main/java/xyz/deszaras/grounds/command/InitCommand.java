package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Creates a new universe and sets it as the current one.
 *
 * Checks: player is GOD; universe name is not "VOID"
 */
public class InitCommand extends Command<Boolean> {

  private final String name;

  public InitCommand(Actor actor, Player player, String name) {
    super(actor, player);
    this.name = Objects.requireNonNull(name);
  }

  @Override
  public Boolean execute() throws CommandException {

    if (Universe.VOID.getName().equals(name)) {
      throw new CommandException("You may not recreate the VOID universe");
    }

    Universe universe = new Universe(name);
    actor.sendMessage(newInfoMessage("Created universe " + universe.getName()));

    Place origin = createOrigin(universe);
    createLostAndFound(universe);

    Universe.setCurrent(universe);
    universe.addThing(player);
    player.setLocation(origin);
    origin.give(player);

    return true;
  }

  private Place createOrigin(Universe universe) {
    Place origin = new Place("ORIGIN");
    universe.addThing(origin);

    origin.setDescription(
        "This is the first place to exist in its new universe. From here" +
        " you can start building more things to create a new world. Type" +
        " `build help` to see what you can create.");

    actor.sendMessage(newInfoMessage("Created origin place " + origin.getId()));
    return origin;
  }

  private void createLostAndFound(Universe universe) {
    Place laf = new Place("LOST+FOUND");
    universe.addThing(laf);
    universe.setLostAndFoundId(laf.getId());

    laf.setDescription(
        "This is where the contents of destroyed things end up.");

    actor.sendMessage(newInfoMessage("Created lost+found place " + laf.getId()));
  }

  public static InitCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String name = commandArgs.get(0);
    return new InitCommand(actor, player, name);
  }
}
