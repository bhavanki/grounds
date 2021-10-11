package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.combat.AddCombatNpcCommand;
import xyz.deszaras.grounds.command.combat.AddCombatPlayerCommand;
import xyz.deszaras.grounds.command.combat.EndCombatCommand;
import xyz.deszaras.grounds.command.combat.InitCombatCommand;
import xyz.deszaras.grounds.command.combat.MoveCombatCommand;
import xyz.deszaras.grounds.command.combat.MoveNpcCombatCommand;
import xyz.deszaras.grounds.command.combat.RemoveCombatNpcCommand;
import xyz.deszaras.grounds.command.combat.RemoveCombatPlayerCommand;
// import xyz.deszaras.grounds.command.combat.ResolveRoundCombatCommand;
import xyz.deszaras.grounds.command.combat.RestoreCombatCommand;
import xyz.deszaras.grounds.command.combat.SaveCombatCommand;
import xyz.deszaras.grounds.command.combat.StartCombatCommand;
import xyz.deszaras.grounds.command.combat.StatusCombatCommand;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.model.MissingThingException;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class CombatCommand extends Command<String> {

  private static final Logger LOG = LoggerFactory.getLogger(CombatCommand.class);

  public CombatCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  protected String executeImpl() throws CommandException {
    throw new UnsupportedOperationException("This is a composite command");
  }

  public static Optional<Combat> findCombat(Place location) {
    for (UUID id : location.getContents()) {
      Optional<Thing> t = Universe.getCurrent().getThing(id);
      if (t.isPresent()) {
        Thing tt = t.get();
        if (tt instanceof Combat) {
          return Optional.of((Combat) tt);
        }
      }
    }

    return Optional.empty();
  }

  public static Combat findCombatOrFail(Place location) throws CommandException {
    return findCombat(location)
        .orElseThrow(() -> new CommandException("No combat is present"));
  }

  public static void messageAllCombatants(Combat combat, String messageText) {
    Player owner;
    try {
      Optional<Thing> ownerOpt = combat.getOwner();
      if (!ownerOpt.isPresent()) {
        LOG.error("Failed to send message to combatants in combat " +
                  combat.getName() + ", no owner");
        return;
      }
      if (!(ownerOpt.get() instanceof Player)) {
        LOG.error("Failed to send message to combatants in combat " +
                  combat.getName() + ", owner " + ownerOpt.get().getName() +
                  " is not a player");
        return;
      }
      owner = (Player) ownerOpt.get();
    } catch (MissingThingException e) {
      LOG.error("Failed to send message to combatants in combat " +
                combat.getName() + ", owner is missing");
      return;
    }
    Message message = new Message(owner, Message.Style.COMBAT, messageText);
    for (Player player : combat.getAllCombatants()) {
      player.sendMessage(message);
    }
  }

  private static final Map<String, Class<? extends Command>> COMBAT_COMMANDS;

  static {
    COMBAT_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("INIT", InitCombatCommand.class)
        .put("END", EndCombatCommand.class)
        .put("ADD", AddCombatPlayerCommand.class)
        .put("ADD_NPC", AddCombatNpcCommand.class)
        .put("REMOVE", RemoveCombatPlayerCommand.class)
        .put("REMOVE_NPC", RemoveCombatNpcCommand.class)
        .put("RESTORE", RestoreCombatCommand.class)
        .put("LOAD", RestoreCombatCommand.class)
        .put("SAVE", SaveCombatCommand.class)
        .put("STATUS", StatusCombatCommand.class)
        .put("START", StartCombatCommand.class)
        .put("MOVE", MoveCombatCommand.class)
        .put("MOVE_NPC", MoveNpcCombatCommand.class)
        .put("M", MoveCombatCommand.class)
        .put("MN", MoveNpcCombatCommand.class)
        // .put("RESOLVE_ROUND", ResolveRoundCombatCommand.class)
        .build();
  }

  private static final CommandFactory COMBAT_COMMAND_FACTORY =
      new CommandFactory(null, COMBAT_COMMANDS, null);

  public static Command newCommand(Actor actor, Player player,
                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return COMBAT_COMMAND_FACTORY.getCommand(actor, player, commandArgs);
  }
}
