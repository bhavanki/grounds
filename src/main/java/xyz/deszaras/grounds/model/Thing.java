package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A thing that exists in the world.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
              include = JsonTypeInfo.As.PROPERTY,
              property = "class")
public class Thing {

  /**
   * The ID of the special thing NOTHING.
   */
  public static final UUID NOTHING_ID = new UUID(0L, 0L);
  /**
   * The special thing NOTHING.
   */
  public static final Thing NOTHING =
      new Thing("NOTHING", Universe.VOID, NOTHING_ID) {};

  /**
   * The ID of the special thing EVERYTHING.
   */
  public static final UUID EVERYTHING_ID = new UUID(0L, 1L);
  /**
   * The special thing EVERYTHING.
   */
  public static final Thing EVERYTHING =
      new Thing("EVERYTHING", Universe.VOID, EVERYTHING_ID) {};

  static {
    Universe.VOID.addThing(NOTHING);
    Universe.VOID.addThing(EVERYTHING);
  }

  private final UUID id;
  private final Map<String, Attr> attrs;
  // private final AccessRuleKeeper accessRuleKeeper;
  private final Set<UUID> contents;

  /**
   * Creates a new thing with a random ID.
   *
   * @param name name
   * @param universe starting universe
   * @throws NullPointerException if any argument is null
   */
  public Thing(String name, Universe universe) {
    this(name, universe, UUID.randomUUID());
  }

  /**
   * Creates a new thing.
   *
   * @param name name
   * @param universe starting universe
   * @param id ID
   * @throws NullPointerException if any argument is null
   */
  public Thing(String name, Universe universe, UUID id) {
    this.id = Objects.requireNonNull(id);
    attrs = new HashMap<>();
    attrs.put(AttrNames.NAME,
              new Attr(AttrNames.NAME,
                       Objects.requireNonNull(name)));
    attrs.put(AttrNames.UNIVERSE,
              new Attr(AttrNames.UNIVERSE,
                       Objects.requireNonNull(universe).getName()));
    // accessRuleKeeper = new AccessRuleKeeper();
    contents = new HashSet<>();
  }

  /**
   * Creates a new thing.
   *
   * @param id ID
   * @param attrs attributes
   * @param contents contents
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if there is no attribute for
   * name or universe name
   */
  @JsonCreator
  public Thing(
      @JsonProperty("id") UUID id,
      @JsonProperty("attrs") Set<Attr> attrs,
      @JsonProperty("contents") Set<UUID> contents) {
    this.id = Objects.requireNonNull(id);
    this.attrs = new HashMap<>();
    Objects.requireNonNull(attrs).stream()
        .forEach(a -> this.attrs.put(a.getName(), a));
    if (!this.attrs.containsKey(AttrNames.NAME)) {
      throw new IllegalArgumentException("Name not defined for thing with ID " + id);
    }
    if (!this.attrs.containsKey(AttrNames.UNIVERSE)) {
      throw new IllegalArgumentException("Universe name not defined for thing with ID " + id);
    }
    this.contents = new HashSet<>(Objects.requireNonNull(contents));
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
   * Gets this thing's thingspec.
   *
   * @return thingspec
   */
  @JsonIgnore
  public String getThingSpec() {
    return getUniverse().getName() + "::" + getId().toString();
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
   * Gets this thing's universe.
   */
  @JsonIgnore
  public Universe getUniverse() {
    String universeName = getAttr(AttrNames.UNIVERSE).get().getValue();
    return Multiverse.MULTIVERSE.getUniverse(universeName);
  }

  /**
   * Gets this thing's owner.
   */
  @JsonIgnore
  public Optional<Thing> getOwner() {
    return getAttr(AttrNames.OWNER).map(a -> Multiverse.MULTIVERSE.findThing(a.getValue()).orElse(null));
  }

  /**
   * Sets this thing's universe.
   */
  public void setUniverse(Universe universe) {
    setAttr(AttrNames.UNIVERSE, Objects.requireNonNull(universe).getName());
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
   * Gets one of this thing's attributes.
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
   * @param value attribute value, as a Boolean
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
   * @param value attribute value, as a Boolean
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
   * @param value attribute value, as a Boolean
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

  // public Map<UUID, Set<AccessRule>> getAccessRules() {
  //   return accessRuleKeeper.getAccessRules();
  // }

  // public synchronized void addAccessRule(AccessRule accessRule) {
  //   accessRuleKeeper.addAccessRule(accessRule);
  // }

  // public void removeAccessRule(AccessRule accessRule) {
  //   accessRuleKeeper.removeAccessRule(accessRule);
  // }

  // public boolean grantsReadAccess(Thing accessor, Attr targetAttr) {
  //   return grantsAccess(accessor, targetAttr, AccessRule.Permission.READ);
  // }

  // public boolean grantsWriteAccess(Thing accessor, Attr targetAttr) {
  //   return grantsAccess(accessor, targetAttr, AccessRule.Permission.WRITE);
  // }

  // public boolean grantsAccess(Thing accessor, Attr targetAttr, AccessRule.Permission permission) {
  //   return accessRuleKeeper.grantsAccess(accessor, targetAttr, permission);
  // }

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
   * Checks if this thing contains the given thing.
   *
   * @param thing other thing
   * @return true if this thing contains the other thing
   */
  public boolean has(Thing thing) {
    synchronized (contentsMonitor) {
      return contents.contains(thing.getId());
    }
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

}
