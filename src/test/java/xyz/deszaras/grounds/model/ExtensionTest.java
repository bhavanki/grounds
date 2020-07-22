package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.command.ScriptedCommand;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptFactory;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ExtensionTest {

  private Extension e;

  private static class TestEvent extends Event<String> {
    private TestEvent(Player player) {
      super(player, "test");
    }
  }

  @BeforeEach
  public void setUp() {
    e = new Extension("extension");
  }

  @Test
  public void testHandle() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("scriptContent", "script")));
    e.setAttr(listenerAttr);

    ScriptFactory scriptFactory = mock(ScriptFactory.class);
    Script script = mock(Script.class);
    when(scriptFactory.newScript(listenerAttr, e)).thenReturn(script);
    when(script.getExtension()).thenReturn(e);

    Event event = new TestEvent(Player.GOD);

    CommandExecutor commandExecutor = mock(CommandExecutor.class);

    e.handle(event, scriptFactory, commandExecutor);

    verify(commandExecutor).submit(any(ScriptedCommand.class));
  }

  @Test
  public void testHandleSelectingEventType() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("scriptContent", "script"),
                new Attr("eventType", "TestEvent")));
    e.setAttr(listenerAttr);

    ScriptFactory scriptFactory = mock(ScriptFactory.class);
    Script script = mock(Script.class);
    when(scriptFactory.newScript(listenerAttr, e)).thenReturn(script);
    when(script.getExtension()).thenReturn(e);

    Event event = new TestEvent(Player.GOD);

    CommandExecutor commandExecutor = mock(CommandExecutor.class);

    e.handle(event, scriptFactory, commandExecutor);

    verify(commandExecutor).submit(any(ScriptedCommand.class));
  }

  @Test
  public void testHandleNotSelectingEventType() throws Exception {
    Attr listenerAttr = new Attr("^listener1",
        List.of(new Attr("scriptContent", "script"),
                new Attr("eventType", "NotTestEvent")));
    e.setAttr(listenerAttr);

    ScriptFactory scriptFactory = mock(ScriptFactory.class);
    Script script = mock(Script.class);
    when(scriptFactory.newScript(listenerAttr, e)).thenReturn(script);
    when(script.getExtension()).thenReturn(e);

    Event event = new TestEvent(Player.GOD);

    CommandExecutor commandExecutor = mock(CommandExecutor.class);

    e.handle(event, scriptFactory, commandExecutor);

    verify(commandExecutor, never()).submit(any(ScriptedCommand.class));
  }

  @Test
  public void testGetListenerAttrs() {
    Attr listenerAttr = new Attr("^listener1", List.of(new Attr("scriptContent", "script")));
    e.setAttr(listenerAttr);

    e.setAttr("notAListener", "hi");
    e.setAttr("^listener0", "wrongtype");

    assertEquals(Set.of(listenerAttr), e.getListenerAttrs());
  }
}
