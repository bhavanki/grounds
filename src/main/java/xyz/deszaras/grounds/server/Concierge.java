package xyz.deszaras.grounds.server;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.BuildCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.CommandResult;
import xyz.deszaras.grounds.command.DestroyCommand;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Universe;

/**
 * A guest manager. Get it?
 */
class Concierge {

  private static final Logger LOG = LoggerFactory.getLogger(Concierge.class);

  private final Universe universe;
  private final CommandExecutor commandExecutor;

  /**
   * Creates a new concierge.
   *
   * @param universe universe where guest players reside
   * @param commandExecutor command executor for manipulating guests
   */
  Concierge(Universe universe, CommandExecutor commandExecutor) {
    this.universe = Objects.requireNonNull(universe);
    this.commandExecutor = Objects.requireNonNull(commandExecutor);
  }

  /**
   * All Concierge instances share this same global counter.
   */
  private static AtomicInteger guestCounter = new AtomicInteger();
  // one day, maybe should be scoped to server instead

  @VisibleForTesting
  static void resetGuestCounter() {
    guestCounter.set(0);
  }

  /**
   * Generates a new unique name for a guest player.
   *
   * @return guest name
   */
  private static String generateGuestName() {
    return String.format("guest%d", guestCounter.incrementAndGet());
  }

  /**
   * Builds a new guest player.
   *
   * @return guest player, or null if one could not be created
   */
  Player buildGuestPlayer() {
    String guestName = generateGuestName();
    Command<String> buildCommand = new BuildCommand(Actor.ROOT, Player.GOD, "player", guestName,
                                                    List.of("guest"));
    Future<CommandResult<String>> buildCommandFuture = commandExecutor.submit(buildCommand);
    String guestPlayerId;
    try {
      CommandResult<String> buildCommandResult = buildCommandFuture.get();

      if (!buildCommandResult.isSuccessful()) {
        ((Optional<CommandException>) buildCommandResult.getCommandException())
            .ifPresent(e -> LOG.error(e.getMessage()));
        return null;
      }
      guestPlayerId = buildCommandResult.getResult();
    } catch (ExecutionException e) {
      LOG.error("Building of guest {} failed", guestName, e.getCause());
      return null;
    } catch (InterruptedException e) {
      LOG.error("Interrupted while building guest {}", guestName);
      return null;
    }

    Player guestPlayer = universe.getThing(guestPlayerId, Player.class).get();
    guestPlayer.setCurrentActor(Actor.GUEST);
    guestPlayer.setHome(universe.getGuestHomePlace());
    return guestPlayer;
  }

  /**
   * Destroys a guest player.
   *
   * @param player guest player to destroy
   * @throws IllegalArgumentException if the player is not a guest
   */
  void destroyGuestPlayer(Player player) {
    if (!player.trySetCurrentActor(null, Actor.GUEST)) {
      throw new IllegalArgumentException(String.format("Actor for guest player %s is not a guest",
                                                       player.getName()));
    }

    Command destroyCommand = new DestroyCommand(Actor.ROOT, Player.GOD, player);
    Future<CommandResult<Boolean>> destroyCommandFuture = commandExecutor.submit(destroyCommand);
    try {
      CommandResult<Boolean> destroyCommandResult = destroyCommandFuture.get();

      if (!destroyCommandResult.isSuccessful()) {
        ((Optional<CommandException>) destroyCommandResult.getCommandException())
            .ifPresent(e -> LOG.error(e.getMessage()));
      }
    } catch (ExecutionException e) {
      LOG.error("Destruction of guest {} failed", player.getName(), e.getCause());
    } catch (InterruptedException e) {
      LOG.error("Interrupted while destroying guest {}", player.getName());
    }
  }
}
