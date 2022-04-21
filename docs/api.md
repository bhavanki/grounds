# API

The Grounds API is a limited interface for plugins to use in order to interact with the universe beyond their results.

Like calls to plugins themselves, API calls to Grounds are also conducted using JSON RPC. A plugin opens a connection to the Unix domain socket that Grounds listens on and writes a JSON RPC request to it, and receives a JSON RPC response from it. API calls are synchronous.

By default, the API domain socket is at _/tmp/groundsapi.sock_, but it may be configured to a different location.

## API JSON RPC Request

The total size of an API request may not exceed 4096 bytes.

The request method defines which API method to run.

The request ID is a random value. The JSON RPC response returned by Grounds must use the matching ID to be valid.

The request params is a JSON object with fields that depend on the API method being run. The following additional fields are supported.

<table>
  <tr><th>Name</th><th>Type</th><th>Purpose</th></tr>
  <tr>
    <td>_plugin_call_id</td><td>string</td><td>(required) Unique identifier for the current plugin call</td>
  </tr>
  <tr>
    <td>_as_extension</td><td>bool</td><td>true to run as the plugin's extension</td>
  </tr>
</table>

When a plugin calls back to the Grounds API, it has to supply its plugin call ID so that Grounds can retrieve that context and satisfy the API call. Without this param, Grounds rejects the call.

By default, API methods run on behalf of the player who called the plugin (which is in turn calling the API). If the `_as_extension` parameter is set to true, and a method supports it, then the method is instead run on behalf of the extension where the plugin command or listener attribute is defined. Use this mechanism to allow a plugin to perform tasks that a player cannot otherwise do, such as manipulate attributes on the extension.

## API JSON RPC Response

If the API call is considered successful, Grounds provides a result and no error. The type of the result depends on the method that was run.

If the API call is considered a failure, then Grounds provides an error and no result. The error contains an error code and a message, but no data.

## API Methods

In these method descriptions, the "caller" is the player who executed the plugin command which is in turn calling the Grounds API. If a method indicates that it can run as an extension, then the caller is instead the extension where the plugin command resides.

### exec

Executes the given command line, as if the caller had entered it into their own shell. This is the primary means for plugins to work with the game universe.

Under the hood, Grounds executes only one command at a time. Commands run using `exec` are run as part of their encompassing plugin command, so a plugin does not need to wait for results, and no other commands will interfere. However, a plugin should not spend too long executing commands, so that the server can respond to commands from other players.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>commandLine</td>
    <td>array of string</td>
    <td>(required) command line to execute</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">string form of the command result</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">command line execution failed</td>
  </tr>
</table>

### getAttr

Gets an attribute with the given name from the referenced thing.

See the [attributes documentation](attributes.md) for the attribute JSON definition.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>thingId</td>
    <td>string</td>
    <td>(required) thing ID</td>
  </tr>
  <tr>
    <td>name</td>
    <td>string</td>
    <td>(required) attribute name</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">attribute JSON</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32004</td><td colspan="2">the attribute is not found</td>
  </tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">getting the attribute failed</td>
  </tr>
</table>

### getAttrNames

Lists the names of all attributes of the referenced thing.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>thingId</td>
    <td>string</td>
    <td>(required) thing ID</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">attribute names as an array of strings</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">getting the attribute names failed</td>
  </tr>
</table>

### getCallerName

Gets the name of the plugin caller.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr><td colspan="3">may *not* be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">caller name as a string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr><td colspan="3">none</td></tr>
</table>

### getCallerTimezone

Gets the timezone of the plugin caller, if their actor has set one. If the caller has no actor, or the actor has not set a timezone, returns "UTC".

<table>
  <tr><th colspan="3">params</th></tr>
  <tr><td colspan="3">may *not* be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">caller timezone as a string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr><td colspan="3">none</td></tr>
</table>

### getRoles

Lists the roles of the referenced player. Either the player's ID or name must be supplied.

Remember that the GOD player has no roles, but a plugin should allow GOD to do anything.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>thingId</td>
    <td>string</td>
    <td>player ID</td>
  </tr>
  <tr>
    <td>playerName</td>
    <td>string</td>
    <td>player name</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">roles as an array of strings</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32004</td><td colspan="2">the player is not found</td>
  </tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">getting the roles failed</td>
  </tr>
</table>

### removeAttr

Removes an attribute with the given name from the referenced thing.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>thingId</td>
    <td>string</td>
    <td>(required) thing ID</td>
  </tr>
  <tr>
    <td>name</td>
    <td>string</td>
    <td>(required) attribute name</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">empty string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">getting the attribute failed</td>
  </tr>
</table>

### sendMessage

Sends the given message from the caller to the named player.

A message can take one of three forms, and exactly one of the forms must be provided. The simplest is an ordinary message string, supplied with `message`.

To format the message as a set of key-value pairs, pass JSON under the `record`param. Here is an example of how the JSON must be formatted.

```
{
  "keys": ["key1", "key2", "key3"],
  "values": ["value1", "value2", "value3"]
}
```

If a key is empty, then its value is printed alone.

To format the message as a table, pass JSON under the `table` param. Here is an example of how the JSON must be formatted. The format strings in column definitions are [Java format strings](https://www.baeldung.com/java-string-formatter).

```
{
  "columns": [
    [
      "column1 string",
      "%s"
    ],
    [
      "column2 with special formatting for row values",
      "%s",
      "%40s"
    ]
  ],
  "rows": [
    [
      "row1",
      "42"
    ],
    [
      "row2",
      "43"
    ],
    [
      "row3",
      "44"
    ]
  ]
}
```
[Markup](markup.md) is supported for the following message pieces:

* the entire string when `message` is used
* elements of the "values" array when `record` is used
* all elements of "rows" when `table` is used

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>playerName</td>
    <td>string</td>
    <td>(required) player to send message to</td>
  </tr>
  <tr>
    <td>message</td>
    <td>string</td>
    <td>plain message string (may use markup)</td>
  </tr>
  <tr>
    <td>record</td>
    <td>string</td>
    <td>record JSON to use as message</td>
  </tr>
  <tr>
    <td>table</td>
    <td>string</td>
    <td>table JSON to use as message</td>
  </tr>
  <tr>
    <td>header</td>
    <td>string</td>
    <td>header line to emit before message (may use markup)</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">empty string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type, or if record or table JSON is invalid</td>
  </tr>
</table>

### sendMessageToCaller

Sends the given message from the caller to the caller. See `sendMessage` for details about how to specify a message.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>message</td>
    <td>string</td>
    <td>plain message string (may use markup)</td>
  </tr>
  <tr>
    <td>record</td>
    <td>string</td>
    <td>record JSON to use as message</td>
  </tr>
  <tr>
    <td>table</td>
    <td>string</td>
    <td>table JSON to use as message</td>
  </tr>
  <tr>
    <td>header</td>
    <td>string</td>
    <td>header line to emit before message (may use markup)</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">empty string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type, or if record or table JSON is invalid</td>
  </tr>
</table>

### setAttr

Sets an attribute with the given name from the referenced thing.

See the [attributes documentation](attributes.md) for the list of valid types and, for each type, how its value should be represented as a string.

<table>
  <tr><th colspan="3">params</th></tr>
  <tr>
    <td>thingId</td>
    <td>string</td>
    <td>(required) thing ID</td>
  </tr>
  <tr>
    <td>name</td>
    <td>string</td>
    <td>(required) attribute name</td>
  </tr>
  <tr>
    <td>value</td>
    <td>string</td>
    <td>(required) attribute value</td>
  </tr>
  <tr>
    <td>type</td>
    <td>string</td>
    <td>(required) attribute type</td>
  </tr>
  <tr><td colspan="3">may be run as an extension</td></tr>
  <tr><th colspan="3">result</th></tr>
  <tr>
    <td colspan="3">empty string</td>
  </tr>
  <tr><th colspan="3">error codes</th></tr>
  <tr>
    <td>-32602</td><td colspan="2">a required param is missing, or a param is the wrong type</td>
  </tr>
  <tr>
    <td>-32603</td><td colspan="2">getting the attribute failed</td>
  </tr>
</table>
