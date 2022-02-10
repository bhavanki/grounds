package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Works with actor preferences.<p>
 *
 * Arguments: preference string
 */
@PermittedRoles(roles = { Role.DENIZEN, Role.BARD, Role.ADEPT, Role.THAUMATURGE })
public class PreferenceCommand extends Command<String> {

  private final String prefString;
  private final boolean save;

  public PreferenceCommand(Actor actor, Player player, String prefString) {
    this(actor, player, prefString, true);
  }

  @VisibleForTesting
  public PreferenceCommand(Actor actor, Player player, String prefString, boolean save) {
    super(actor, player);
    this.prefString = prefString;
    this.save = save;
  }

  @Override
  protected String executeImpl() throws CommandException {
    if (prefString == null) {
      return format(actor.getPreferences());
    }

    if (!prefString.contains("=") || prefString.indexOf("=") == 0) {
      throw new CommandException("Invalid preference string " + prefString);
    }
    String[] prefParts = prefString.split("=", 2);
    String prefName = prefParts[0];
    String prefValue = prefParts[1];

    boolean result;
    if (!prefValue.isEmpty()) {
      result = ActorDatabase.INSTANCE.updateActorRecord(actor.getUsername(),
          r -> r.setPreference(prefName, prefValue));
    } else {
      result = ActorDatabase.INSTANCE.updateActorRecord(actor.getUsername(),
          r -> r.setPreference(prefName, null));
    }
    if (!result) {
      throw new CommandException("I could not find the actor named " +
                                 actor.getUsername());
    }

    if (save) {
      ActorCommand.saveActorDatabase();
    }

    if (!prefValue.isEmpty()) {
      actor.setPreference(prefName, prefValue);
    } else {
      actor.setPreference(prefName, null);
    }

    return format(actor.getPreferences());
  }

  private static String format(Map<String, String> prefs) {
    return prefs.keySet().stream()
        .sorted()
        .map(k -> String.format("%s = %s", k, prefs.get(k)))
        .collect(Collectors.joining(", "));
  }

  public static PreferenceCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs) {
    String prefString = commandArgs.size() > 0 ? commandArgs.get(0) : null;
    return new PreferenceCommand(actor, player, prefString);
  }
}
