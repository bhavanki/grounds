package xyz.deszaras.grounds.command;

import java.util.List;

import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

/**
 * Works with actor preferences.<p>
 *
 * Arguments: preference string
 */
public class PreferenceCommand extends Command<String> {

  private final String prefString;

  public PreferenceCommand(Actor actor, Player player, String prefString) {
    super(actor, player);
    this.prefString = prefString;
  }

  @Override
  public String execute() throws CommandException {
    if (prefString == null) {
      return actor.getPreferences().toString();
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

    ActorCommand.saveActorDatabase();

    if (!prefValue.isEmpty()) {
      actor.setPreference(prefName, prefValue);
    } else {
      actor.setPreference(prefName, null);
    }

    return actor.getPreferences().toString();
  }

  public static PreferenceCommand newCommand(Actor actor, Player player,
                                             List<String> commandArgs) {
    String prefString = commandArgs.size() > 0 ? commandArgs.get(0) : null;
    return new PreferenceCommand(actor, player, prefString);
  }
}
