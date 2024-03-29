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
import xyz.deszaras.grounds.command.CommandFactoryException;
import xyz.deszaras.grounds.command.Event;
import xyz.deszaras.grounds.command.PluginCallCommand;

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
    handle(event, CommandExecutor.getInstance());
  }

  /**
   * Handles an event sent from the command event bus.
   *
   * @param event           event to handle
   * @param commandExecutor command executor for resulting commands
   */
  @VisibleForTesting
  void handle(Event event, CommandExecutor commandExecutor) {
    Set<Attr> listenerAttrs = getListenerAttrs();
    for (Attr a : listenerAttrs) {

      // If the attribute has an "eventType" attribute in its list, check if the
      // simple class name of the event matches it. If not, do nothing.
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

      // If the attribute has a "localized" attribute in its list, check if the
      // event's place is the same as this extension's location, and do nothing
      // if they don't match.
      Optional<Attr> localizedAttr = a.getAttrListValue().stream()
          .filter(la -> la.getName().equals("localized") &&
                        la.getType() == Attr.Type.BOOLEAN)
          .findFirst();
      if (localizedAttr.isPresent() && localizedAttr.get().getBooleanValue() &&
          event.getLocation() != null) {
        try {
          Optional<Thing> extensionLocation = getLocation();
          if (extensionLocation.isEmpty()) {
            LOG.warn("Extension {} has no location, cannot check if event " +
                     "of type {} is local", getName(), event.getClass());
          } else if (!extensionLocation.get().equals(event.getLocation())) {
            LOG.debug("Listener attribute {} of extension {} wants localized " +
                      "event, but received event from {}, so not creating command",
                      a.getName(), getName(), event.getLocation().getName());
            continue;
          }
        } catch (MissingThingException e) {
          LOG.warn("Extension {} has a missing location, cannot check if event " +
                   "of type {} is local", getName(), event.getClass());
        }
      }

      // Create a plugin call command for the listener attribute. Pass the
      // augmented event payload JSON string as the sole argument. Then, submit
      // the command to be run later. This is asynchronous, so this handler
      // should return reasonably quickly.
      try {
        PluginCallCommand command = commandExecutor.getCommandFactory()
            .newPluginCallCommand(Actor.INTERNAL, this, a, this,
                                  List.of(event.getAugmentedPayloadJsonString()));
        LOG.debug("Submitting plugin call command for listener {}", a.getName());
        commandExecutor.submit(command);
      } catch (CommandFactoryException e) {
        LOG.error("Failed to create plugin call command for listener attribute {} on {}",
                  a.getName(), getName(), e);
      }
    }
  }

  /**
   * Gets all of the listener attributes for this extension. A listener
   * attribute has a name starting with a caret and is a list type.
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
