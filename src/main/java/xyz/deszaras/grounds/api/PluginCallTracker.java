package xyz.deszaras.grounds.api;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;

/**
 * A tracker for plugin calls. An API call must ultimately have originated due
 * to some call to a plugin from Grounds itself. That call needs to get a unique
 * ID. The tracker remembers the ID and context around the plugin call, like
 * who is running the command making the call. When Grounds receives an API
 * call, it can retrieve the context from the tracker, so that the API method
 * can use it (for example, to know who the actor is).
 */
class PluginCallTracker {

  private final ConcurrentHashMap<String,PluginCallInfo> pluginCalls;

  /**
   * Creates a new tracker.
   */
  PluginCallTracker() {
    pluginCalls = new ConcurrentHashMap<>();
  }

  /**
   * Tracks a plugin call.
   *
   * @param id   plugin call ID
   * @param info plugin call info
   */
  public void track(String id, PluginCallInfo info) {
    pluginCalls.put(id, info);
  }

  /**
   * Gets information about a plugin call.
   *
   * @param  id plugin call ID
   * @return    plugin call info
   */
  public Optional<PluginCallInfo> getInfo(String id) {
    return Optional.ofNullable(pluginCalls.get(id));
  }

  /**
   * Stops tracking a plugin call.
   *
   * @param id plugin call ID
   */
  public void untrack(String id) {
    pluginCalls.remove(id);
  }

  /**
   * Information about a plugin call that a tracker remembers.
   */
  static class PluginCallInfo {
    private final Actor actor;
    private final Player caller;
    // TBD extension

    /**
     * Creates a new object.
     *
     * @param actor  actor
     * @param caller caller
     */
    PluginCallInfo(Actor actor, Player caller) {
      this.actor = actor;
      this.caller = caller;
    }

    /**
     * Gets the actor.
     *
     * @return actor
     */
    Actor getActor() {
      return actor;
    }

    /**
     * Gets the caller.
     *
     * @return caller
     */
    Player getCaller() {
      return caller;
    }
  }
}

