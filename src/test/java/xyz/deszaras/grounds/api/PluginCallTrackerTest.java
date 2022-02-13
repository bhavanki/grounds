package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.api.PluginCallTracker.PluginCallInfo;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;

public class PluginCallTrackerTest {

  private Actor actor;
  private Player runner;
  private PluginCallTracker tracker;

  @BeforeEach
  public void setUp() throws Exception {
    tracker = new PluginCallTracker();
    actor = new Actor("bob");
    runner = new Player("jay");
  }

  @Test
  public void testTracking() {
    assertTrue(tracker.getInfo("id").isEmpty());

    PluginCallInfo info = new PluginCallInfo(actor, runner);
    tracker.track("id", info);

    PluginCallInfo info2 = tracker.getInfo("id").get();
    assertEquals(actor, info.getActor());
    assertEquals(runner, info.getRunner());

    tracker.untrack("id");
    assertTrue(tracker.getInfo("id").isEmpty());
  }
}
