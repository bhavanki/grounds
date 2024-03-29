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

    Place origin = updateOrigin(universe);
    updateLostAndFound(universe);
    updateGuestHome(universe);

    Universe.setCurrent(universe);
    Universe.setCurrentFile(null);
    universe.addThing(player);
    player.setLocation(origin);
    origin.give(player);

    return true;
  }

  private Place updateOrigin(Universe universe) {
    Place origin = universe.getOriginPlace();

    origin.setDescription(
        "This is the first place to exist in its new universe. From here" +
        " you can start building more things to create a new world. Type" +
        " `help build` to see what you can create.");

    player.sendMessage(newInfoMessage("Updated origin place " + origin.getId()));
    return origin;
  }

  private void updateLostAndFound(Universe universe) {
    Place laf = universe.getLostAndFoundPlace();

    laf.setDescription(
        "This is where the contents of destroyed things end up.");

    player.sendMessage(newInfoMessage("Updated lost+found place " + laf.getId()));
  }

  private void updateGuestHome(Universe universe) {
    Place ghome = universe.getGuestHomePlace();

    ghome.setDescription(
        "Welcome, guests! This is the guest lounge, where you may talk with " +
        "players and other guests, and perhaps do a little exploring. Try " +
        "these commands to get started.\n\n" +
        "- `look` to see where you are and who is also here\n" +
        "- `say Hi` to say \"Hi\" to everyone here\n" +
        "- `help commands` to learn about other available commands\n" +
        "- `exit` to log out of the server");

    Policy ghPolicy = ghome.getPolicy();
    ghPolicy.setRoles(Policy.Category.GENERAL, Role.ALL_ROLES);
    ghPolicy.setRoles(Policy.Category.READ, Role.ALL_ROLES);

    player.sendMessage(newInfoMessage("Updated guest home place " + ghome.getId()));
  }

  public static InitCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    String name = commandArgs.get(0);
    return new InitCommand(actor, player, name);
  }
}
