package xyz.deszaras.grounds.model;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
// import xyz.deszaras.grounds.command.CommandExecutor;
// import xyz.deszaras.grounds.command.Message;
// import xyz.deszaras.grounds.util.CommandLineUtils;

/**
 * A thing that exists in a universe.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
              include = JsonTypeInfo.As.PROPERTY,
              property = "class")
public class Thing {

  private static final Logger LOG = LoggerFactory.getLogger(Thing.class);

  /**
   * The ID of the special thing NOTHING.
   */
  public static final UUID NOTHING_ID = new UUID(0L, 0L);
  /**
   * The special thing NOTHING.
   */
  public static final Thing NOTHING =
      new Thing("NOTHING", NOTHING_ID) {};

  /**
   * The ID of the special thing EVERYTHING.
   */
  public static final UUID EVERYTHING_ID = new UUID(0L, 1L);
  /**
   * The special thing EVERYTHING.
   */
  public static final Thing EVERYTHING =
      new Thing("EVERYTHING", EVERYTHING_ID) {};

  private final UUID id;
  private final Map<String, Attr> attrs; // key = attr name
  private final Set<UUID> contents;
  private final Policy policy;

  /**
   * Creates a new thing with a random ID.
   *
   * @param name name
   * @throws NullPointerException if name is null
   */
  public Thing(String name) {
    this(name, UUID.randomUUID());
  }

  /**
   * Creates a new thing.
   *
   * @param name name
   * @param id ID
   * @throws NullPointerException if any argument is null
   */
  public Thing(String name, UUID id) {
    this.id = Objects.requireNonNull(id);
    attrs = new HashMap<>();
    attrs.put(AttrNames.NAME,
              new Attr(AttrNames.NAME,
                       Objects.requireNonNull(name)));
    contents = new HashSet<>();
    policy = new Policy(Policy.DEFAULT);
  }

  /**
   * Creates a new thing.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @param policy policy
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if there is no attribute for name
   */
  @JsonCreator
  public Thing(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents,
      @JsonProperty("policy") Policy policy) {
    this.id = Objects.requireNonNull(id);
    this.attrs = new HashMap<>();
    Objects.requireNonNull(attrs).stream()
        .forEach(a -> this.attrs.put(a.getName(), a));
    if (!this.attrs.containsKey(AttrNames.NAME)) {
      throw new IllegalArgumentException("Name not defined for thing with ID " + id);
    }
    this.contents = new HashSet<>(Objects.requireNonNull(contents));
    this.policy = new Policy(Objects.requireNonNull(policy));
  }

  /**
   * Gets this thing's ID.
   *
   * @return ID
   */
  @JsonProperty
  public UUID getId() {
    return id;
  }

  /**
   * Gets this thing's name.
   *
   * @return name
   */
  @JsonIgnore
  public String getName() {
    return getAttr(AttrNames.NAME).get().getValue();
  }

  /**
   * Gets this thing's description.
   *
   * @return description
   */
  @JsonIgnore
  public Optional<String> getDescription() {
    return getAttr(AttrNames.DESCRIPTION).map(a -> a.getValue());
  }

  /**
   * Sets this thing's description. Pass a null description to remove
   * it.
   *
   * @param description description
   */
  public void setDescription(String description) {
    if (description != null) {
      setAttr(AttrNames.DESCRIPTION, description);
    } else {
      removeAttr(AttrNames.DESCRIPTION);
    }
  }

  /**
   * Gets this thing's location.
   *
   * @return location, or empty if there is none
   * @throws MissingThingException if a location is set but not in the universe
   */
  @JsonIgnore
  public Optional<Thing> getLocation() throws MissingThingException {
    Optional<Attr> locationAttr =  getAttr(AttrNames.LOCATION);
    if (locationAttr.isEmpty()) {
      return Optional.empty();
    }
    Optional<Thing> location =
        Universe.getCurrent().getThing(locationAttr.get().getValue());
    if (location.isEmpty()) {
      throw new MissingThingException("Location is missing");
    }
    return location;
  }

  /**
   * Sets this thing's location. Pass a null location to remove it.
   */
  public void setLocation(Thing location) {
    if (location != null) {
      setAttr(AttrNames.LOCATION, location);
    } else {
      removeAttr(AttrNames.LOCATION);
    }
  }

  /**
   * Gets this thing's owner.
   *
   * @return owner, or empty if there is none
   * @throws MissingThingException if an owner is set but not in the universe
   */
  @JsonIgnore
  public Optional<Thing> getOwner() throws MissingThingException {
    Optional<Attr> ownerAttr =  getAttr(AttrNames.OWNER);
    if (ownerAttr.isEmpty()) {
      return Optional.empty();
    }
    Optional<Thing> owner =
        Universe.getCurrent().getThing(ownerAttr.get().getValue());
    if (owner.isEmpty()) {
      throw new MissingThingException("Owner is missing");
    }
    return owner;
  }

  /**
   * Sets this thing's owner. Pass a null owner to remove it.
   */
  public void setOwner(Thing owner) {
    if (owner != null) {
      setAttr(AttrNames.OWNER, owner);
    } else {
      removeAttr(AttrNames.OWNER);
    }
  }

  /**
   * Gets this thing's home.
   *
   * @return home place, or empty if a home is not set
   * @throws MissingThingException if a home is set but not in the universe
   */
  @JsonIgnore
  public Optional<Place> getHome() throws MissingThingException {
    Optional<Attr> homeAttr =  getAttr(AttrNames.HOME);
    if (homeAttr.isEmpty()) {
      return Optional.empty();
    }
    Optional<Place> home =
        Universe.getCurrent().getThing(homeAttr.get().getValue(), Place.class);
    if (home.isEmpty()) {
      throw new MissingThingException("Home is missing");
    }
    return home;
  }

  /**
   * Sets this thing's home. Pass a null home to remove it.
   */
  public void setHome(Place home) {
    if (home != null) {
      setAttr(AttrNames.HOME, home);
    } else {
      removeAttr(AttrNames.HOME);
    }
  }

  /**
   * Gets this thing's mute list.
   *
   * @return mute list, with any missing things in the list omitted
   */
  @JsonIgnore
  public List<Thing> getMuteList() {
    List<Thing> muteList = new ArrayList<Thing>();

    Optional<Attr> muteAttr = getAttr(AttrNames.MUTE);
    if (muteAttr.isEmpty() || muteAttr.get().getValue().isEmpty()) {
      return muteList;
    }

    for (String id : muteAttr.get().getValue().split(",")) {
      Optional<Thing> mutee = Universe.getCurrent().getThing(id);
      if (mutee.isPresent()) {
        muteList.add(mutee.get());
      }
    }
    return muteList;
  }

  /**
   * Sets this thing's mute list. Pass a null or empty list to unmute
   * everything.
   *
   * @param muteList mute list
   */
  public void setMuteList(List<Thing> muteList) {
    if (muteList == null) {
      removeAttr(AttrNames.MUTE);
    } else {
      setAttr(AttrNames.MUTE, muteList.stream()
                  .map(t -> t.getId().toString())
                  .collect(Collectors.joining(",")));
    }
  }

  /**
   * Checks if this thing mutes the given thing. Prefer this method
   * over {@link #getMuteList()} when possible, because this one
   * does not require looking up each thing in the universe.
   *
   * @param  thing thing to check
   * @return       true if thing is muted
   */
  public boolean mutes(Thing thing) {
    Optional<Attr> muteAttr = getAttr(AttrNames.MUTE);
    if (muteAttr.isEmpty() || muteAttr.get().getValue().isEmpty()) {
      return false;
    }

    String thingId = thing.getId().toString();
    return Arrays.stream(muteAttr.get().getValue().split(","))
        .anyMatch(id -> thingId.equals(id));
  }

  /**
   * Gets this thing's mailbox.
   *
   * @return mailbox, or empty if there is none
   * @throws MissingThingException if a mailbox is set but not in the universe
   */
  @JsonIgnore
  public Optional<Thing> getMailbox() throws MissingThingException {
    Optional<Attr> mailboxAttr =  getAttr(AttrNames.MAILBOX);
    if (mailboxAttr.isEmpty()) {
      return Optional.empty();
    }
    Optional<Thing> mailbox =
        Universe.getCurrent().getThing(mailboxAttr.get().getValue());
    if (mailbox.isEmpty()) {
      throw new MissingThingException("Mailbox is missing");
    }
    return mailbox;
  }

  /**
   * Sets this thing's mailbox. Pass a null mailbox to remove it.
   */
  public void setMailbox(Thing mailbox) {
    if (mailbox != null) {
      setAttr(AttrNames.MAILBOX, mailbox);
    } else {
      removeAttr(AttrNames.MAILBOX);
    }
  }

  private final Object attrMonitor = new Object();

  /**
   * Gets an immutable set of this thing's attributes.
   *
   * @return attributes
   */
  @JsonProperty
  public final Set<Attr> getAttrs() {
    synchronized (attrMonitor) {
      return ImmutableSet.copyOf(attrs.values());
    }
  }

  /**
   * Gets one of this thing's attributes by name.
   *
   * @param name attribute name
   * @return attribute, or null if not present
   */
  public final Optional<Attr> getAttr(String name) {
    synchronized (attrMonitor) {
      return Optional.ofNullable(attrs.get(name));
    }
  }

  /**
   * Gets one of this thing's attributes by name and type.
   *
   * @param name attribute name
   * @param type attribute type
   * @return attribute, or null if not present
   */
  public final Optional<Attr> getAttr(String name, Attr.Type type) {
    Optional<Attr> attr = getAttr(name);
    if (attr.isPresent() && attr.get().getType() == type) {
      return attr;
    }
    return Optional.empty();
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param attr attribute to set
   */
  public final void setAttr(Attr attr) {
    synchronized (attrMonitor) {
      attrs.put(attr.getName(), attr);
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as a string
   */
  public final void setAttr(String name, String value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as an integer
   */
  public final void setAttr(String name, int value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as a Boolean
   */
  public final void setAttr(String name, boolean value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as an Instant
   */
  public final void setAttr(String name, Instant value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as a Thing
   */
  public final void setAttr(String name, Thing value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as a Attr
   */
  public final void setAttr(String name, Attr value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  /**
   * Sets an attribute for this thing. If an attribute already
   * exists with the same name, it is replaced.
   *
   * @param name attribute name
   * @param value attribute value, as a list of Attr
   */
  public final void setAttr(String name, List<Attr> value) {
    synchronized (attrMonitor) {
      attrs.put(name, new Attr(name, value));
    }
  }

  public final void incrAttr(String name) {
    addToAttr(name, 1);
  }

  public final void decrAttr(String name) {
    addToAttr(name, -1);
  }

  /**
   * Adds to the value of an attribute for this thing. The value
   * must be an integer type.
   *
   * @param name attribute name
   * @param incr amount to increment (may be negative)
   * @return new attribute value
   * @throws IllegalStateException if this thing has no attribute
   * by the given name, or if the attribute's value is not an
   * integer type
   */
  public final int addToAttr(String name, int incr) {
    synchronized (attrMonitor) {
      if (!attrs.containsKey(name)) {
        throw new IllegalStateException("Attribute " + name + " not found");
      }
      Attr attr = attrs.get(name);
      if (attr.getType() != Attr.Type.INTEGER) {
        throw new IllegalStateException("Attribute " + name + " is type " + attr.getType());
      }
      int newValue = attr.getIntValue() + incr;
      attrs.put(name, new Attr(name, newValue));
      return newValue;
    }
  }

  /**
   * Conditionally sets the Boolean value of an attribute for this
   * thing. The attribute value is set to the given value only if
   * the attribute's current value is as expected.
   *
   * @param name attribute name
   * @param expect expected attribute value
   * @param value new attribute value
   * @return true if the value was set, false if not
   * @throws IllegalStateException if this thing has no attribute
   * by the given name, or if the attribute's value is not an
   * Boolean type
   */
  public final boolean compareAndSetAttr(String name, boolean expect, boolean value) {
    synchronized (attrMonitor) {
      if (!attrs.containsKey(name)) {
        throw new IllegalStateException("Attribute " + name + " not found");
      }
      Attr attr = attrs.get(name);
      if (attr.getType() != Attr.Type.BOOLEAN) {
        throw new IllegalStateException("Attribute " + name + " is type " + attr.getType());
      }
      if (attr.getBooleanValue() != expect) {
        return false;
      }
      attrs.put(name, new Attr(name, value));
      return true;
    }
  }

  /**
   * Gets and sets the Boolean value of an attribute for this
   * thing.
   *
   * @param name attribute name
   * @param value new attribute value
   * @return old attribute value
   * @throws IllegalStateException if this thing has no attribute
   * by the given name, or if the attribute's value is not an
   * Boolean type
   */
  public final boolean getAndSetAttr(String name, boolean value) {
    synchronized (attrMonitor) {
      if (!attrs.containsKey(name)) {
        throw new IllegalStateException("Attribute " + name + " not found");
      }
      Attr attr = attrs.get(name);
      if (attr.getType() != Attr.Type.BOOLEAN) {
        throw new IllegalStateException("Attribute " + name + " is type " + attr.getType());
      }
      boolean oldValue = attr.getBooleanValue();
      attrs.put(name, new Attr(name, value));
      return oldValue;
    }
  }

  /**
   * Removes an attribute from this thing.
   *
   * @param name attribute name
   */
  public final void removeAttr(String name) {
    synchronized (attrMonitor) {
      attrs.remove(name);
    }
  }

  private final Object contentsMonitor = new Object();

  /**
   * Gets the contents of this thing.
   *
   * @return contents
   */
  @JsonProperty
  public Set<UUID> getContents() {
    return ImmutableSet.copyOf(contents);
  }

  /**
   * Checks if this thing directly contains the given thing.
   *
   * @param thingId ID of other thing
   * @return true if this thing contains the other thing
   */
  public boolean has(UUID thingId) {
    synchronized (contentsMonitor) {
      return contents.contains(thingId);
    }
  }

  /**
   * Checks if this thing directly contains the given thing.
   *
   * @param thing other thing
   * @return true if this thing contains the other thing
   */
  public boolean has(Thing thing) {
    return has(thing.getId());
  }

  /**
   * Gives the given thing to this thing.
   *
   * @param thing other thing
   */
  public void give(Thing thing) {
    synchronized (contentsMonitor) {
      contents.add(thing.getId());
    }
  }

  /**
   * Takes the given thing from this thing.
   *
   * @param thing other thing
   */
  public void take(Thing thing) {
    synchronized (contentsMonitor) {
      contents.remove(thing.getId());
    }
  }

  /**
   * Gets the policy of this thing. This is not a defensive copy.
   *
   * @return policy
   */
  @JsonProperty
  public Policy getPolicy() {
    return policy;
  }

  /**
   * Checks if the player is permitted for a category, according to this
   * thing's policy. GOD is always permitted. A player always passes a
   * category for themselves.
   *
   * @param category category to check permission for
   * @param player player to check permission for
   * @return true if the player is permitted for the category
   */
  @SuppressWarnings("PMD.EmptyCatchBlock")
  public boolean passes(Policy.Category category, Player player) {
    if (player.equals(Player.GOD)) {
      LOG.debug("Permission check: category {}, player GOD, result true",
                category);
      return true;
    }
    if (player.equals(this)) {
      LOG.debug("Permission check: category {}, player self, result true",
                category);
      return true;
    }
    Set<Role> playerRoles = new HashSet<>(Universe.getCurrent().getRoles(player));
    try {
      if (player.equals(getOwner().orElse(null))) {
        playerRoles.add(Role.OWNER);
      }
    } catch (MissingThingException e) {
      // don't add the role
    }
    boolean result = policy.passes(category, playerRoles);
    LOG.debug("Permission check: category {}, player {}/{}, roles {}, result {}",
              category, player.getName(), player.getId(), playerRoles, result);
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof Thing)) {
      return false;
    }

    return ((Thing) other).getId().equals(id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);

  @JsonIgnore
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to convert thing to JSON", e);
    }
  }

  public static Thing build(String name, List<String> buildArgs) {
    checkArgument(buildArgs.size() == 0, "Expected 0 build arguments, got " + buildArgs.size());
    return new Thing(name);
  }
}
