package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import xyz.deszaras.grounds.command.combat.AddCombatPlayerCommand;
import xyz.deszaras.grounds.command.combat.EndCombatCommand;
import xyz.deszaras.grounds.command.combat.InitCombatCommand;
// import xyz.deszaras.grounds.command.combat.MoveCombatCommand;
import xyz.deszaras.grounds.command.combat.RemoveCombatPlayerCommand;
// import xyz.deszaras.grounds.command.combat.ResolveRoundCombatCommand;
import xyz.deszaras.grounds.command.combat.StartCombatCommand;
import xyz.deszaras.grounds.command.combat.StatusCombatCommand;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class CombatCommand extends Command<String> {

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

  private static final Map<String, Class<? extends Command>> COMBAT_COMMANDS;

  static {
    COMBAT_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("INIT", InitCombatCommand.class)
        .put("END", EndCombatCommand.class)
        .put("ADD", AddCombatPlayerCommand.class)
        .put("REMOVE", RemoveCombatPlayerCommand.class)
        .put("STATUS", StatusCombatCommand.class)
        .put("START", StartCombatCommand.class)
        // .put("MOVE", MoveCombatCommand.class)
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
