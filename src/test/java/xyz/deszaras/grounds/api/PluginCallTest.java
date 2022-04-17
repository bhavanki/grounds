package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ResourceBundle;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Extension;

public class PluginCallTest {

  static final String PLUGIN_PATH = "path/to/plugin";
  static final String PLUGIN_METHOD = "doTheThing";
  static final Set<Role> PLUGIN_CALLER_ROLES = Set.of(Role.ADEPT, Role.THAUMATURGE);
  static final ResourceBundle PLUGIN_HELP_BUNDLE = mock(ResourceBundle.class);
  static final Extension PLUGIN_EXTENSION = mock(Extension.class);

  private PluginCallTracker tracker;
  private PluginCall call;

  @BeforeEach
  public void setUp() {
    tracker = new PluginCallTracker();
    call = new PluginCall(PLUGIN_PATH, PLUGIN_METHOD, PLUGIN_CALLER_ROLES,
                          PLUGIN_HELP_BUNDLE, PLUGIN_EXTENSION, tracker);
  }

  @Test
  public void testGetters() {
    assertEquals(PLUGIN_PATH, call.getPluginPath());
    assertEquals(PLUGIN_METHOD, call.getMethod());
    assertEquals(PLUGIN_CALLER_ROLES, call.getCallerRoles());
    assertEquals(PLUGIN_HELP_BUNDLE, call.getHelpBundle());
    assertEquals(PLUGIN_EXTENSION, call.getExtension());
    assertEquals(tracker, call.getPluginCallTracker());
  }

  @Test
  public void testToString() {
    assertEquals(PLUGIN_PATH + "::" + PLUGIN_METHOD, call.toString());
  }
}
