package xyz.deszaras.grounds.command;

import java.util.List;
import java.util.Objects;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * Creates a new universe and sets it as the current one.
 */
@PermittedRoles(roles = {})
public class InitCommand extends Command<Boolean> {

  private final String name;

  public InitCommand(Actor actor, Player player, String name) {
    super(actor, player);
    this.name = Objects.requireNonNull(name);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    if (!player.equals(Player.GOD)) {
      throw new PermissionException("Only GOD may create a universe");
    }

    if (Universe.VOID.getName().equals(name)) {
      throw new CommandException("You may not recreate the VOID universe");
    }

    Universe universe = new Universe(name);
    player.sendMessage(newInfoMessage("Created universe " + universe.getName()));

    Place origin = createOrigin(universe);
    createLostAndFound(universe);
    createGuestLounge(universe);

    Universe.setCurrent(universe);
    Universe.setCurrentFile(null);
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
        " `help build` to see what you can create.");

    player.sendMessage(newInfoMessage("Created origin place " + origin.getId()));
    return origin;
  }

  private void createLostAndFound(Universe universe) {
    Place laf = new Place("LOST+FOUND");
    universe.addThing(laf);
    universe.setLostAndFoundId(laf.getId());

    laf.setDescription(
        "This is where the contents of destroyed things end up.");

    player.sendMessage(newInfoMessage("Created lost+found place " + laf.getId()));
  }

  private void createGuestLounge(Universe universe) {
    Place glounge = new Place("GUEST LOUNGE");
    universe.addThing(glounge);
    universe.setGuestHomeId(glounge.getId());

    glounge.setDescription(
        "Welcome, guests! This is the guest lounge, where you may talk with " +
        "players and other guests, and perhaps do a little exploring. Try " +
        "these commands to get started.\n\n" +
        "- `look` to see where you are and who is also here\n" +
        "- `say Hi` to say \"Hi\" to everyone here\n" +
        "- `help commands` to learn about other available commands\n" +
        "- `exit` to log out of the server");

    Policy glPolicy = glounge.getPolicy();
    glPolicy.setRoles(Policy.Category.GENERAL, Role.ALL_ROLES);
    glPolicy.setRoles(Policy.Category.READ, Role.ALL_ROLES);

    player.sendMessage(newInfoMessage("Created guest lounge place " + glounge.getId()));
  }

  public static InitCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String name = commandArgs.get(0);
    return new InitCommand(actor, player, name);
  }
}
