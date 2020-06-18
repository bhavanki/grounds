package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.deszaras.grounds.command.actor.AddActorCommand;
import xyz.deszaras.grounds.command.actor.AddPlayerToActorCommand;
import xyz.deszaras.grounds.command.actor.GetActorCommand;
import xyz.deszaras.grounds.command.actor.RemoveActorCommand;
import xyz.deszaras.grounds.command.actor.RemovePlayerFromActorCommand;
import xyz.deszaras.grounds.command.actor.SetActorPasswordCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

public class ActorCommand extends Command<Boolean> {

  private static final Logger LOG = LoggerFactory.getLogger(ActorCommand.class);

  public ActorCommand(Actor actor, Player player) {
    super(actor, player);
  }

  @Override
  public Boolean execute() {
    throw new UnsupportedOperationException("This is a composite command");
  }

  public static void checkIfRoot(Actor actor, String username) throws CommandException {
    if (Actor.ROOT.getUsername().equals(username)) {
      throw new CommandException("Sorry, you may not work with the root actor");
    }
  }

  public static void checkIfGod(Player player) throws PermissionException {
    if (!player.equals(Player.GOD)) {
      throw new PermissionException("Only GOD may work with actors");
    }
  }

  public static boolean saveActorDatabase() throws CommandException {
    try {
      ActorDatabase.INSTANCE.save();
      return true;
    } catch (IOException e) {
      LOG.error("Failed to save actor database", e);
      throw new CommandException("Failed to save actor database, check the logs");
    }
  }

  private static final Map<String, Class<? extends Command>> ACTOR_COMMANDS;

  static {
    ACTOR_COMMANDS = ImmutableMap.<String, Class<? extends Command>>builder()
        .put("CREATE", AddActorCommand.class)
        .put("ADD", AddActorCommand.class)
        .put("SET_PASSWORD", SetActorPasswordCommand.class)
        .put("PASSWORD", SetActorPasswordCommand.class)
        .put("GET", GetActorCommand.class)
        .put("ADD_PLAYER", AddPlayerToActorCommand.class)
        .put("REMOVE_PLAYER", RemovePlayerFromActorCommand.class)
        .put("REMOVE", RemoveActorCommand.class)
        .put("DELETE", RemoveActorCommand.class)
        .build();
  }

  private static final CommandFactory ACTOR_COMMAND_FACTORY =
      new CommandFactory(null, ACTOR_COMMANDS, null);

  public static Command newCommand(Actor actor, Player player,
                                   List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 1);
    return ACTOR_COMMAND_FACTORY.getCommand(actor, player, commandArgs);
  }
}
