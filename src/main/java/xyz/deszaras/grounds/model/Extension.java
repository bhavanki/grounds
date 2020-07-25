package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandExecutor;
import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.command.ScriptedCommand;
import xyz.deszaras.grounds.script.Script;
import xyz.deszaras.grounds.script.ScriptFactory;
import xyz.deszaras.grounds.script.ScriptFactoryException;

/**
 * A thing that houses extensions to the game.
 */
public class Extension extends Player {

  private static final Logger LOG = LoggerFactory.getLogger(Extension.class);

  /**
   * Only certain roles may work with extensions.
   */
  public static final Set<Role> PERMITTED_ROLES = Set.of(Role.BARD, Role.THAUMATURGE);

  public Extension(String name) {
    super(name);
  }

  /**
   * Creates a new extension.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   */
  @JsonCreator
  public Extension(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    super(id, attrs, contents, policy);
  }

  /**
   * Handles an event sent from the command event bus.
   *
   * @param event event to handle
   */
  @Subscribe
  public void handle(Event event) {
    LOG.debug("Extension {} handling event of type {}", getName(), event.getClass());
    handle(event, new ScriptFactory(), CommandExecutor.getInstance());
  }

  /**
   * Handles an event sent from the command event bus.
   *
   * @param event event to handle
   * @param scriptFactory script factor used to build each listener attribute script
   * @param commandExecutor command executor for resulting scripted commands
   */
  @VisibleForTesting
  void handle(Event event, ScriptFactory scriptFactory, CommandExecutor commandExecutor) {
    Set<Attr> listenerAttrs = getListenerAttrs();
    for (Attr a : listenerAttrs) {

      // If the attribute has an "eventType" attribute in its list, check if the
      // simple class name of the event matches it. If not, do nothing. This
      // avoids needlessly parsing the listener attribute's script and
      // submitting it as a command when the script doesn't care about the
      // event.
      Optional<Attr> eventTypeAttr = a.getAttrListValue().stream()
          .filter(la -> la.getName().equals("eventType") &&
                        la.getType() == Attr.Type.STRING)
          .findFirst();
      if (eventTypeAttr.isPresent() &&
          !eventTypeAttr.get().getValue().equals(event.getClass().getSimpleName())){
        LOG.debug("Listener attribute {} of extension {} wants event type {}, " +
                  "but received {}, so not creating command",
                  a.getName(), getName(), eventTypeAttr.get().getValue(),
                  event.getClass());
        continue;
      }

      // For similar reasons, if the attribute has a "localized" attribute in
      // its list, check if the event's place is the same as this extension's
      // location, and do nothing if they don't match.
      Optional<Attr> localizedAttr = a.getAttrListValue().stream()
          .filter(la -> la.getName().equals("localized") &&
                        la.getType() == Attr.Type.BOOLEAN)
          .findFirst();
      if (localizedAttr.isPresent() && localizedAttr.get().getBooleanValue() &&
          event.getPlace() != null) {
        try {
          Optional<Thing> extensionLocation = getLocation();
          if (extensionLocation.isEmpty()) {
            LOG.warn("Extension {} has no location, cannot check if event " +
                     "of type {} is local", getName(), event.getClass());
          } else if (!extensionLocation.get().equals(event.getPlace())) {
            LOG.debug("Listener attribute {} of extension {} wants localized " +
                      "event, but received event from {}, so not creating command",
                      a.getName(), getName(), event.getPlace().getName());
            continue;
          }
        } catch (MissingThingException e) {
          LOG.warn("Extension {} has a missing location, cannot check if event " +
                   "of type {} is local", getName(), event.getClass());
        }
      }

      // Create a scripted command for the listener attribute's script. Pass the
      // augmented event payload JSON string as the sole argument. Then, submit
      // the command to be run later. This is asynchronous, so this handler
      // should return reasonably quickly.
      try {
        Script listenerScript = scriptFactory.newScript(a, this);
        ScriptedCommand command =
            new ScriptedCommand(Actor.INTERNAL, this, listenerScript,
                                List.of(event.getAugmentedPayloadJsonString()));
        LOG.debug("Submitting scripted command for listener {}", a.getName());
        commandExecutor.submit(command);
      } catch (ScriptFactoryException e) {
        LOG.error("Failed to create script for listener attribute {} on {}",
                  a.getName(), getName(), e);
      }
    }
  }

  /**
   * Gets all of the listener attributes for this extension. A listener
   * attribute has a name starting with a caret and is a list type. It must have
   * a "scriptContent" attribute for what it should run when called.
   *
   * @return listener attributes for this extension
   */
  @VisibleForTesting
  Set<Attr> getListenerAttrs() {
    return getAttrs().stream()
        .filter(a -> a.getName().startsWith("^") && a.getType() == Attr.Type.ATTRLIST)
        .collect(Collectors.toSet());
  }

  public static Extension build(String name, List<String> buildArgs) {
    Extension extension = new Extension(name);
    return extension;
  }
}
