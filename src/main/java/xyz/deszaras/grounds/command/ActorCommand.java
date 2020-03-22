package xyz.deszaras.grounds.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;
import xyz.deszaras.grounds.server.HashedPasswordAuthenticator;

public class ActorCommand extends Command {

  private final String actionString;
  private final List<String> actorArgs;

  public ActorCommand(Actor actor, Player player, String actionString,
                      List<String> actorArgs) {
    super(actor, player);
    this.actionString = Objects.requireNonNull(actionString);
    this.actorArgs = ImmutableList.copyOf(Objects.requireNonNull(actorArgs));
  }

  @Override
  public boolean execute() {
    if (!player.equals(Player.GOD)) {
      actor.sendMessage("Only GOD may work with actors");
      return false;
    }

    String username;
    String password;
    UUID playerId;
    boolean result;
    switch (actionString.toUpperCase()) {
      case "CREATE":
      case "ADD":
        ensureExactActorArgs(actorArgs, 2, "creating");
        username = actorArgs.get(0);
        if (!checkIfRoot(username)) {
          result = false;
          break;
        }
        password = actorArgs.get(1);
        result =
            ActorDatabase.INSTANCE.createActorRecord(
                username,
                HashedPasswordAuthenticator.hashPassword(password));
        if (!result) {
          actor.sendMessage("An actor named " + username + " already exists");
        }
        break;
      case "SET_PASSWORD":
      case "PASSWORD":
        ensureExactActorArgs(actorArgs, 2, "setting password for");
        username = actorArgs.get(0);
        if (!checkIfRoot(username)) {
          result = false;
          break;
        }
        password = actorArgs.get(1);
        result = ActorDatabase.INSTANCE.updateActorRecord(
            username,
            r -> r.setPassword(HashedPasswordAuthenticator.hashPassword(password)));
        if (!result) {
          actor.sendMessage("I could not find the actor named " + username);
        }
        break;
      case "ADD_PLAYER":
        ensureExactActorArgs(actorArgs, 2, "adding player for");
        username = actorArgs.get(0);
        if (!checkIfRoot(username)) {
          result = false;
          break;
        }
        playerId = UUID.fromString(actorArgs.get(1));
        result = ActorDatabase.INSTANCE.updateActorRecord(
            username,
            r -> r.addPlayer(playerId));
        if (!result) {
          actor.sendMessage("I could not find the actor named " + username);
        }
        break;
      case "REMOVE_PLAYER":
        ensureExactActorArgs(actorArgs, 2, "removing player for");
        username = actorArgs.get(0);
        if (!checkIfRoot(username)) {
          result = false;
          break;
        }
        playerId = UUID.fromString(actorArgs.get(1));
        result = ActorDatabase.INSTANCE.updateActorRecord(
            username,
            r -> r.removePlayer(playerId));
        if (!result) {
          actor.sendMessage("I could not find the actor named " + username);
        }
        break;
      case "GET":
        ensureExactActorArgs(actorArgs, 1, "getting");
        username = actorArgs.get(0);
        checkIfRoot(username);
        Optional<ActorRecord> actorRecord =
            ActorDatabase.INSTANCE.getActorRecord(username);
        if (actorRecord.isPresent()) {
          actor.sendMessage(actorRecord.get().toString());
          result = true;
        } else {
          actor.sendMessage("I could not find the actor named " + username);
          result = false;
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported actor command " + actionString);
    }

    return true;
  }

  private static void ensureExactActorArgs(List<String> l, int n, String action) {
    if (l.size() != n) {
      throw new IllegalArgumentException("For " + action + " actor, expected " + n +
                                         " arguments, got " + l.size());
    }
  }

  private boolean checkIfRoot(String username) {
    if (Actor.ROOT.getUsername().equals(username)) {
      actor.sendMessage("Sorry, you may not work with the root actor");
      return false;
    }
    return true;
  }

  public static ActorCommand newCommand(Actor actor, Player player,
                                        List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    String actionString = commandArgs.get(0);
    List<String> actorArgs = commandArgs.subList(1, commandArgs.size());
    return new ActorCommand(actor, player, actionString, actorArgs);
  }

}
