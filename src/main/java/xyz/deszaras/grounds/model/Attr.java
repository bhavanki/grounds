package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An attribute, which is a name/value pair. The value has a specific
 * type.
 */
public final class Attr {

  /**
   * Attribute types.
   */
  public enum Type {
    STRING,
    INTEGER,
    BOOLEAN,
    THING,
    ATTR,
    ATTRLIST;
  }
  private final String name;
  private final String value;
  private final Type type;

  @JsonCreator
  private Attr(
      @JsonProperty("name") String name,
      @JsonProperty("value") String value,
      @JsonProperty("type") Type type) {
    this.name = Objects.requireNonNull(name);
    this.value = Objects.requireNonNull(value);
    this.type = type;
  }

  /**
   * Creates a new string attribute.
   *
   * @param name attribute name
   * @param value attribute value
   */
  public Attr(String name, String value) {
    this(name, value, Type.STRING);
  }

  /**
   * Creates a new Boolean attribute.
   *
   * @param name attribute name
   * @param value attribute value
   */
  public Attr(String name, boolean value) {
    this(name, value ? "true" : "false", Type.BOOLEAN);
  }

  /**
   * Creates a new integer attribute.
   *
   * @param name attribute name
   * @param value attribute value
   */
  public Attr(String name, int value) {
    this(name, Integer.toString(value, 10), Type.INTEGER);
  }

  /**
   * Creates a new thing attribute.
   *
   * @param name attribute name
   * @param thing attribute value
   */
  public Attr(String name, Thing thing) {
    this(name, thing.getThingSpec(), Type.THING);
  }

  /**
   * Creates a new attr attribute.
   *
   * @param name attribute name
   * @param attr attribute value
   */
  public Attr(String name, Attr attr) {
    this(name, attr.toJson(), Type.ATTR);
  }

  /**
   * Creates a new attrlist attribute.
   *
   * @param name attribute name
   * @param attrList attribute value
   */
  public Attr(String name, List<Attr> attr) {
    this(name, toJson(attr), Type.ATTRLIST);
  }

  /**
   * Gets the name of this attribute.
   *
   * @return attribute name
   */
  @JsonProperty
  public String getName() {
    return name;
  }

  /**
   * Gets the type of this attribute.
   *
   * return attribute type
   */
  @JsonProperty
  public Type getType() {
    return type;
  }

  /**
   * Gets the value of this attribute as a string. This is always
   * possible, even if the value is of a different type.
   *
   * @return attribute value as a string
   */
  @JsonProperty
  public String getValue() {
    return value;
  }

  /**
   * Gets the value of this attribute as an integer.
   *
   * @return attribute value as a integer
   * @throws IllegalStateException if this attribute is not an integer type
   */
  @JsonIgnore
  public int getIntValue() {
    if (type != Type.INTEGER) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return Integer.parseInt(value);
  }

  /**
   * Gets the value of this attribute as a Boolean.
   *
   * @return attribute value as a Boolean
   * @throws IllegalStateException if this attribute is not a Boolean type
   */
  @JsonIgnore
  public boolean getBooleanValue() {
    if (type != Type.BOOLEAN) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return Boolean.parseBoolean(value);
  }

  /**
   * Gets the value of this attribute as a thing.
   *
   * @return attribute value as a thing
   * @throws IllegalStateException if this attribute is not a thing type
   */
  @JsonIgnore
  public Optional<Thing> getThingValue() {
    if (type != Type.THING) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return Multiverse.MULTIVERSE.findThing(value);
  }

  /**
   * Gets the value of this attribute as a thing of a particular class.
   *
   * @param thingClass expected class of thing
   * @return attribute value as a thing of a particular class
   * @throws IllegalStateException if this attribute is not a thing type,
   * or if the value is not of the expected thing class
   */
  @JsonIgnore
  public <T extends Thing> Optional<T> getThingValue(Class<T> thingClass) {
    if (type != Type.THING) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return Multiverse.MULTIVERSE.findThing(value, thingClass);
  }

  /**
   * Gets the value of this attribute as another attribute.
   *
   * @return attribute value as another attribute
   * @throws IllegalStateException if this attribute is not an attr type
   */
  @JsonIgnore
  public Attr getAttrValue() {
    if (type != Type.ATTR) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return fromJson(value);
  }

  /**
   * Gets the value of this attribute as a list of attributes.
   *
   * @return attribute value as a list of attributes
   * @throws IllegalStateException if this attribute is not an attrlist type
   */
  @JsonIgnore
  public List<Attr> getAttrListValue() {
    if (type != Type.ATTRLIST) {
      throw new IllegalStateException("Attribute " + name + " is type " + type);
    }
    return listFromJson(value);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!other.getClass().equals(Attr.class)) {
      return false;
    }

    Attr o = (Attr) other;
    return name.equals(o.name) && type.equals(o.type) && value.equals(o.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, value);
  }

  private static final Pattern ATTR_SPEC_PATTERN =
      Pattern.compile("([^\\[]+)\\[([^]]+)\\]=(.*)");

  public static Attr fromAttrSpec(String attrSpec) {
    Matcher m = ATTR_SPEC_PATTERN.matcher(attrSpec);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid attrSpec " + attrSpec);
    }
    String name = m.group(1);
    Type type = Type.valueOf(m.group(2));
    String value = m.group(3);
    return new Attr(name, value, type);
  }

  public static String buildAttrSpec(String name, Type type, String value) {
    return String.format("%s[%s]=%s", name, type.toString(), value);
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Creates a JSON representation of this attribute.
   *
   * @return JSON string
   * @throws IllegalStateException if conversion fails
   */
  @JsonIgnore
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to convert attribute to JSON", e);
    }
  }

  /**
   * Creates a JSON representation of a list of attributes.
   *
   * @return JSON string
   * @throws IllegalStateException if conversion fails
   */
  public static String toJson(List<Attr> attrList) {
    try {
      return OBJECT_MAPPER.writeValueAsString(attrList);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to convert attribute list to JSON", e);
    }
  }

  /**
   * Creates a new attribute from a JSON representation.
   *
   * @param s JSON string
   * @return attribute
   * @throws IllegalArgumentException if conversion fails
   */
  public static Attr fromJson(String s) {
    try {
      return OBJECT_MAPPER.readValue(s, Attr.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to create attribute from JSON", e);
    }
  }

  /**
   * Creates a new list of attributes from a JSON representation.
   *
   * @param s JSON string
   * @return attribute list
   * @throws IllegalArgumentException if conversion fails
   */
  public static List<Attr> listFromJson(String s) {
    try {
      return OBJECT_MAPPER.readValue(s, new TypeReference<List<Attr>>(){});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to create attribute list from JSON", e);
    }
  }
}
