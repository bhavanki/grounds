# Attributes

Every thing in a Grounds universe has a set of attributes. Attributes are used for a wide variety of built-in features, and can be used for custom features or other game mechanics as well.

A non-wizard player usually doesn't need to deal with attribute structure, but this information is essential for wizards who build out a universe.

## Attribute Structure

An attribute is made up of three things:

* a name
* a type
* a value

The name and value are always just strings. The type can be one of the following values:

* STRING = an ordinary string, like "blue"
* INTEGER = a whole number, like -1, 0, or 42
* BOOLEAN = true or false
* TIMESTAMP = an instant in time
* THING = a (reference to a) thing in the universe
* ATTR = a nested attribute
* ATTRLIST = a list of attributes

Despite all of these different types, the actual value for an attribute is always a string. But, when the attribute type is not STRING, then Grounds has different expectations for what that string value actually contains.

## Attribute Types

### STRING

A string attribute can have any string value, even an empty one. No other attribute type may have an empty value.

### INTEGER

An integer attribute must have an integer, in decimal string form, as its value. Zero and negative numbers are fine, but fractional parts are not permitted (for example, "3.14").

### BOOLEAN

A Boolean attribute may only have value "true" or "false".

### TIMESTAMP

A timestamp attribute indicates an instant in time with the number of seconds since the "epoch", which is January 1, 1970 at midnight UTC. This is a common way to specify a moment in time down to the second. When Grounds displays a timestamp attribute to a player, it converts the time to their timezone if they provided one.

### THING

A thing attribute contains the ID of a thing in the universe. It's usually expected that there is actually a thing in the universe with the ID, but this isn't necessarily always the case. While you can define a thing attribute with any ID you wish, errors may result from it.

### ATTR

An attr attribute contains another attribute in JSON format (see below). You can nest attributes in multiple levels in this way. It is not possible to have an attribute contain itself, though. since it isn't possible to write that in JSON.

### ATTRLIST

An attrlist attribute contains a list of zero or more attributes in JSON format (see below). The nested attributes may be different types, and any type is permitted, including further attr or attrlist attributes.

## JSON Format

An attribute can be represented in [JSON]() as a trio of strings. Here is a simple example.

```
{
  "name": "favoriteColor",
  "type": "STRING",
  "value": "blue"
}
```

An attr attribute has a value with the JSON representation of the nested attribute. Internally, Grounds strips unnecessary whitespace from JSON, so the literal string value it reports for a nested attribute won't have line breaks.

```
{
  "name": "funFact",
  "type": "ATTR",
  "value": "{\"name\":\"favoriteColor\",\"type\":\"STRING\",\"value\":\"blue\"}"
}
```

Similarly, an attrlist attribute has a value with a JSON array of nested attributes. (The example below is abbreviated for readability.)

```
{
  "name": "funFacts",
  "type": "ATTRLIST",
  "value": "[{\"name\":\"favoriteColor\",\"type\":\"STRING\",\"value\":\"blue\"},...]"
}
```

### Setting from a JSON or YAML File

It is hard to manually type in multi-line attribute values or to define nested attributes. As an alternative, the SET_ATTR command accepts the path to a JSON or YAML file that defines an attribute value. The file may make use of whitespace and comments for easier readability and maintenance.

```
- name: favoriteColor
  type: STRING
  value: blue
- name: favoriteNumber
  type: INTEGER
  value: "42"
```

To use such a file, provide an attribute value as the `@` symbol followed by the path to it.

```
$ set_attr me funFacts[ATTRLIST]=@myfunfacts.yaml
```
