package xyz.deszaras.grounds.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An attribute, which is a name/value pair. The value has a specific
 * type.
 */
public final class Attr {

  private static final Logger LOG = LoggerFactory.getLogger(Attr.class);

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
    this.type = type != null ? type : Type.STRING;
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
    this(name, thing.getId().toString(), Type.THING);
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
   * Gets the value of this attribute as a thing. This returns the string form
   * of the thing's ID.
   *
   * @return attribute value as a thing ID
   * @throws IllegalStateException if this attribute is not a thing type
   */
  @JsonIgnore
  public String getThingValue() {
     if (type != Type.THING) {
       throw new IllegalStateException("Attribute " + name + " is type " + type);
     }
     return value;
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

  /**
   * Gets the value of this attribute as a map of attributes.
   *
   * @return attribute value as a map of attributes, keyed by attr name
   * @throws IllegalStateException if this attribute is not an attrlist type
   */
  @JsonIgnore
  public Map<String, Attr> getAttrListValueAsMap() {
    return getAttrListValue().stream().collect(Collectors.toMap(a -> a.getName(), a -> a));
  }

  /**
   * Gets an attribute from the value of this attribute, which is expected to be
   * a list of attributes. In other words, looks through the list of attributes
   * that is the value of this attribute for one with the specified name. An
   * example: if this attribute has a value which is a list of attributes named
   * "a", "b", and "c", then {@code getAttrInAttrListValue("a")} will return the
   * attribute in that list named "a".
   *
   * @param  name name of attribute in list of attributes to get
   * @return      attribute
   * @throws IllegalStateException if this attribute is not an attrlist type
   */
  public Optional<Attr> getAttrInAttrListValue(String name) {
    return getAttrListValue().stream().filter(a -> a.getName().equals(name)).findFirst();
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

  /**
   * Returns the spec string for this attribute.
   *
   * @return attribute spec string
   * @see #fromAttrSpec(String)
   */
  public String toAttrSpec() {
    return String.format("%s[%s]=%s", name, type.toString(), value);
  }

  private static final Pattern ATTR_SPEC_PATTERN =
      Pattern.compile("([^\\[]+)\\[([^]]+)\\]=(.*)",
                      Pattern.DOTALL);
  private static final Pattern STRING_ATTR_SPEC_PATTERN =
      Pattern.compile("([^\\[]+)=(.*)", Pattern.DOTALL);

  private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  /**
   * Creates an attribute from a spec string, formatted as:<p>
   *
   * name[type]=value<p>
   *
   * where "type" is a value of {@link Attr.Type}. "value" is the
   * string value of the attribute; for ATTR and ATTRLIST type
   * attributes, the value is the JSON representation of the
   * attribute or list of attributes, respectively.<p>
   *
   * The "[type]" portion may be omitted, in which case the type
   * is assumed to be {@code Attr.Type.STRING}.
   *
   * If the value begins with the '@' character, then the rest of
   * the value is treated as the path to a file, and the content
   * of the file is loaded and used as the attribute value. For
   * ATTR and ATTRLIST type attributes, the content must be either
   * valid JSON or valid YAML that can reduce to JSON.
   *
   * @param attrSpec attribute spec string
   * @return attribute
   * @throws IllegalArgumentException if the spec string is invalid,
   * or the value cannot be loaded from a file
   */
  public static Attr fromAttrSpec(String attrSpec) {
    String name;
    Type type;
    String value;

    Matcher m = ATTR_SPEC_PATTERN.matcher(attrSpec);
    if (m.matches()) {
      name = m.group(1);
      type = Type.valueOf(m.group(2));
      value = m.group(3);
    } else {
      m = STRING_ATTR_SPEC_PATTERN.matcher(attrSpec);
      if (m.matches()) {
        name = m.group(1);
        type = Type.STRING;
        value = m.group(2);
      } else {
        throw new IllegalArgumentException("Invalid attrSpec " + attrSpec);
      }
    }

    if (value.startsWith("@") && value.length() > 1) {
      try {
        String fileValue = Files.readString(FileSystems.getDefault().getPath(value.substring(1)),
                                            StandardCharsets.UTF_8);
        if (type == Type.ATTR || type == Type.ATTRLIST) {
          JsonNode yamlValue = YAML_OBJECT_MAPPER.readTree(fileValue);
          value = OBJECT_MAPPER.writeValueAsString(yamlValue);
        } else {
          value = fileValue;
        }
      } catch (IOException e) {
        LOG.error("Failed to read attribute value from " + value, e);
        throw new IllegalArgumentException("Failed to read attribute value from " + value, e);
      }
    }
    return new Attr(name, value, type);
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
   * Creates a new attribute from a JSON or YAML representation.
   *
   * @param s JSON or YAML string
   * @return attribute
   * @throws IllegalArgumentException if conversion fails
   */
  public static Attr fromJson(String s) {
    try {
      return YAML_OBJECT_MAPPER.readValue(s, Attr.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to create attribute from JSON", e);
    }
  }

  /**
   * Creates a new list of attributes from a JSON or YAML representation.
   *
   * @param s JSON or YAML string
   * @return attribute list
   * @throws IllegalArgumentException if conversion fails
   */
  public static List<Attr> listFromJson(String s) {
    try {
      return YAML_OBJECT_MAPPER.readValue(s, new TypeReference<List<Attr>>(){});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to create attribute list from JSON", e);
    }
  }
}
