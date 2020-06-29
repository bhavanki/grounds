package xyz.deszaras.grounds.command.actor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.ActorCommand;
import xyz.deszaras.grounds.command.Command;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.PermittedRoles;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Locks an actor, so that they cannot log in until some time in the future.<p>
 *
 * Arguments: username and time to lock until<br>
 * Checks: player is GOD, actor is not ROOT
 */
@PermittedRoles(roles = { Role.ADEPT, Role.THAUMATURGE })
public class LockActorCommand extends Command<Boolean> {

  private final String username;
  private final Instant lockedUntil;

  public LockActorCommand(Actor actor, Player player, String username,
                          Instant lockedUntil) {
    super(actor, player);
    this.username = Objects.requireNonNull(username);
    this.lockedUntil = Objects.requireNonNull(lockedUntil);
  }

  @Override
  protected Boolean executeImpl() throws CommandException {
    ActorCommand.checkIfRoot(player, username);

    boolean result = ActorDatabase.INSTANCE.updateActorRecord(username,
        r -> r.setLockedUntil(lockedUntil));
    if (!result) {
      throw new CommandException("I could not find the actor named " + username);
    }

    return ActorCommand.saveActorDatabase();
  }

  private static final DateTimeFormatter LOCKED_UNTIL_PARSER =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());

  public static LockActorCommand newCommand(Actor actor, Player player,
                                            List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    String username = commandArgs.get(0);
    Instant lockedUntil;
    try {
      lockedUntil = LOCKED_UNTIL_PARSER.parse(commandArgs.get(1), Instant::from);
    } catch (DateTimeParseException e) {
      throw new CommandFactoryException("Failed to parse lockedUntil time", e);
    }
    return new LockActorCommand(actor, player, username, lockedUntil);
  }
}
