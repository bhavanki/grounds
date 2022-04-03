package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.command.PluginCallCommand;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ExtensionTest {

  private Universe universe;
  private Extension e;

  private static class TestPayload {
    public final String s;

    private TestPayload(String s) {
      this.s = s;
    }
  }

  private static class TestEvent extends Event<TestPayload> {
    private TestEvent(Player player, Place place) {
      super(player, place, new TestPayload("test"));
    }
  }

  @BeforeEach
  public void setUp() {
    universe = new Universe("test");
    Universe.setCurrent(universe);

    e = new Extension("extension");
  }

  @Test
  public void testHandle() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit")));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);

    PluginCallCommand pluginCallCommand = mock(PluginCallCommand.class);
    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);
    when(commandExecutor.getCommandFactory()
         .newPluginCallCommand(eq(Actor.INTERNAL), eq(e),
                               eq(listenerAttr), eq(e), any(List.class)))
        .thenReturn(pluginCallCommand);

    e.handle(event, commandExecutor);

    verify(commandExecutor).submit(pluginCallCommand);
    ArgumentCaptor<List> pluginArgumentsCaptor = ArgumentCaptor.forClass(List.class);
    verify(commandExecutor.getCommandFactory())
        .newPluginCallCommand(eq(Actor.INTERNAL), eq(e),
                              eq(listenerAttr), eq(e), pluginArgumentsCaptor.capture());
    List<String> pluginArguments = pluginArgumentsCaptor.getValue();
    assertEquals(1, pluginArguments.size());
    assertEquals(event.getAugmentedPayloadJsonString(), pluginArguments.get(0));
  }

  @Test
  public void testHandleSelectingEventType() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit"),
                new Attr("eventType", "TestEvent")));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);

    PluginCallCommand pluginCallCommand = mock(PluginCallCommand.class);
    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);
    when(commandExecutor.getCommandFactory()
         .newPluginCallCommand(eq(Actor.INTERNAL), eq(e),
                               eq(listenerAttr), eq(e), any(List.class)))
        .thenReturn(pluginCallCommand);

    e.handle(event, commandExecutor);

    verify(commandExecutor).submit(pluginCallCommand);
  }

  @Test
  public void testHandleSelectingEventTypeReject() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit"),
                new Attr("eventType", "NotTestEvent")));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);

    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);

    e.handle(event, commandExecutor);

    verify(commandExecutor, never()).submit(any(PluginCallCommand.class));
  }

  @Test
  public void testHandleLocalized() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit"),
                new Attr("localized", true)));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);
    place.give(e);
    e.setLocation(place);
    universe.addThing(place);
    universe.addThing(e);

    PluginCallCommand pluginCallCommand = mock(PluginCallCommand.class);
    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);
    when(commandExecutor.getCommandFactory()
         .newPluginCallCommand(eq(Actor.INTERNAL), eq(e),
                               eq(listenerAttr), eq(e), any(List.class)))
        .thenReturn(pluginCallCommand);

    e.handle(event, commandExecutor);

    verify(commandExecutor).submit(pluginCallCommand);
  }

  @Test
  public void testHandleLocalizedReject() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit"),
                new Attr("localized", true)));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);
    Place notPlace = new Place("notThere");
    notPlace.give(e);
    e.setLocation(notPlace);
    universe.addThing(place);
    universe.addThing(notPlace);
    universe.addThing(e);

    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);

    e.handle(event, commandExecutor);

    verify(commandExecutor, never()).submit(any(PluginCallCommand.class));
  }

  @Test
  public void testHandleNonLocalized() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("pluginMethod", "doit"),
                new Attr("localized", false)));
    e.setAttr(listenerAttr);

    Place place = new Place("there");
    Event event = new TestEvent(Player.GOD, place);
    Place notPlace = new Place("notThere");
    notPlace.give(e);
    e.setLocation(notPlace);
    universe.addThing(place);
    universe.addThing(notPlace);
    universe.addThing(e);

    PluginCallCommand pluginCallCommand = mock(PluginCallCommand.class);
    CommandExecutor commandExecutor =
        mock(CommandExecutor.class, RETURNS_DEEP_STUBS);
    when(commandExecutor.getCommandFactory()
         .newPluginCallCommand(eq(Actor.INTERNAL), eq(e),
                               eq(listenerAttr), eq(e), any(List.class)))
        .thenReturn(pluginCallCommand);

    e.handle(event, commandExecutor);

    verify(commandExecutor).submit(pluginCallCommand);
  }

  @Test
  public void testGetListenerAttrs() {
    Attr listenerAttr = new Attr("^listener1", List.of(new Attr("pluginMethod", "doit")));
    e.setAttr(listenerAttr);

    e.setAttr("notAListener", "hi");
    e.setAttr("^listener0", "wrongtype");

    assertEquals(Set.of(listenerAttr), e.getListenerAttrs());
  }
}
