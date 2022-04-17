package xyz.deszaras.grounds.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Attr;
import xyz.deszaras.grounds.model.Extension;

public class PluginCallFactoryTest {

  private Attr a;
  private Extension e;
  private PluginCallTracker tracker;

  private PluginCallFactory pcf;
  private PluginCall call;

  @BeforeEach
  public void setUp() {
    e = mock(Extension.class);
    tracker = new PluginCallTracker();

    pcf = new PluginCallFactory();
  }

  @Test
  public void testNewPluginCall() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.PATH, PluginCallTest.PLUGIN_PATH),
      new Attr(PluginCallFactory.METHOD, PluginCallTest.PLUGIN_METHOD),
      new Attr(PluginCallFactory.CALLER_ROLES,
               PluginCallTest.PLUGIN_CALLER_ROLES.stream()
                  .map(r -> r.name())
                  .collect(Collectors.joining(",")))
    ));

    call = pcf.newPluginCall(a, e, tracker);

    assertEquals(PluginCallTest.PLUGIN_PATH, call.getPluginPath());
    assertEquals(PluginCallTest.PLUGIN_METHOD, call.getMethod());
    assertEquals(PluginCallTest.PLUGIN_CALLER_ROLES, call.getCallerRoles());
    assertEquals(e, call.getExtension());
    assertEquals(tracker, call.getPluginCallTracker());

    ResourceBundle helpBundle = call.getHelpBundle();
    assertEquals(0, helpBundle.keySet().size());
  }

  @Test
  public void testNewPluginCallNotAttrList() throws PluginCallFactoryException {
    a = new Attr("$doit", PluginCallTest.PLUGIN_METHOD);

    assertThrows(PluginCallFactoryException.class,
                 () -> pcf.newPluginCall(a, e, tracker));
  }


  @Test
  public void testNewPluginCallMissingPath() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.METHOD, PluginCallTest.PLUGIN_METHOD),
      new Attr(PluginCallFactory.CALLER_ROLES,
               PluginCallTest.PLUGIN_CALLER_ROLES.stream()
                  .map(r -> r.name())
                  .collect(Collectors.joining(",")))
    ));

    assertThrows(PluginCallFactoryException.class,
                 () -> pcf.newPluginCall(a, e, tracker));
  }

  @Test
  public void testNewPluginCallMissingMethod() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.PATH, PluginCallTest.PLUGIN_PATH),
      new Attr(PluginCallFactory.CALLER_ROLES,
               PluginCallTest.PLUGIN_CALLER_ROLES.stream()
                  .map(r -> r.name())
                  .collect(Collectors.joining(",")))
    ));

    assertThrows(PluginCallFactoryException.class,
                 () -> pcf.newPluginCall(a, e, tracker));
  }

  @Test
  public void testNewPluginCallDefaultRoles() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.PATH, PluginCallTest.PLUGIN_PATH),
      new Attr(PluginCallFactory.METHOD, PluginCallTest.PLUGIN_METHOD)
    ));

    call = pcf.newPluginCall(a, e, tracker);

    assertEquals(PluginCallFactory.DEFAULT_CALLER_ROLES, call.getCallerRoles());
  }

  @Test
  public void testNewPluginCallInvalidRoles() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.PATH, PluginCallTest.PLUGIN_PATH),
      new Attr(PluginCallFactory.METHOD, PluginCallTest.PLUGIN_METHOD),
      new Attr(PluginCallFactory.CALLER_ROLES, "BUNNY,BARD")
    ));

    assertThrows(PluginCallFactoryException.class,
                 () -> pcf.newPluginCall(a, e, tracker));
  }


  @Test
  public void testNewPluginCallHelpBundle() throws PluginCallFactoryException {
    a = new Attr("$doit", List.of(
      new Attr(PluginCallFactory.PATH, PluginCallTest.PLUGIN_PATH),
      new Attr(PluginCallFactory.METHOD, PluginCallTest.PLUGIN_METHOD),
      new Attr(PluginCallFactory.HELP, List.of(
        new Attr("$doit_doug", List.of(
          new Attr("syntax", "$DOIT DOUG"),
          new Attr("summary", "Win the game!")
        )),
        new Attr("$doit_brodie", List.of(
          new Attr("syntax", "$DOIT BRODIE"),
          new Attr("summary", "Would you like a pretzel?")
        ))
      ))
    ));

    call = pcf.newPluginCall(a, e, tracker);

    ResourceBundle helpBundle = call.getHelpBundle();
    assertEquals(4, helpBundle.keySet().size());
    assertEquals("$DOIT DOUG", helpBundle.getString("$doit_doug.syntax"));
    assertEquals("Win the game!", helpBundle.getString("$doit_doug.summary"));
    assertEquals("$DOIT BRODIE", helpBundle.getString("$doit_brodie.syntax"));
    assertEquals("Would you like a pretzel?", helpBundle.getString("$doit_brodie.summary"));
  }
}
