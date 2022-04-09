# Plugins

[Extensions](extensions.md) can call plugins to implement plugin commands and listener attributes. First learn about how extensions work before getting into the reference details here about what plugins can do.

## Elements of a Plugin Command

A _plugin command_ is a Grounds command that is implemented with a plugin. It is defined by an extension attribute whose name starts with `$`. The value of the attribute is a list with the following supported attributes.

<table>
  <tr><th>Name</th><th>Type</th><th>Purpose</th></tr>
  <tr>
    <td>pluginPath</td><td>STRING</td><td>(required) Path to the plugin executable</td>
  </tr>
  <tr>
    <td>pluginMethod</td><td>STRING</td><td>(required) JSON RPC request method</td>
  </tr>
  <tr>
    <td>callerRoles</td><td>STRING</td><td>Comma-separated list of permitted roles for callers (default is all non-guest roles)</td>
  </tr>
  <tr>
    <td>commandHelp</td><td>ATTRLIST</td><td>Plugin command help</td>
  </tr>
</table>

The optional "commandHelp" attribute provides help text for the command. When this attribute is present, a player can run the HELP command to see the help text for the command.

The commandHelp attribute itself is an attribute list, and should contain the following three string attributes.

<table>
  <tr><th>Name</th><th>Purpose</th><th>Example</th></tr>
  <tr>
    <td>syntax</td><td>Syntax definition</td><td>$COINFLIP</td>
  </tr>
  <tr>
    <td>summary</td><td>One-line summary of the command</td><td>Flips a coin</td>
  </tr>
  <tr>
    <td>description</td><td>Main help documentation; may be multiple paragraphs</td>
    <td>The outcome of the flip is posed by the command in the caller's name.</td>
  </tr>
</table>

## Plugin JSON RPC Request

Grounds and plugins communicate using [JSON RPC 2.0](https://www.jsonrpc.org/specification). Grounds calls a plugin by running it and supplying a JSON RPC request to it on standard input.

The request method is whatever was specified for the plugin command. A plugin may use this to support multiple plugin commands, one per method.

The request ID is a random value. The JSON RPC response emitted by the plugin must use the matching ID to be valid.

The request params sent by Grounds is a JSON object with the following fields.

<table>
  <tr><th>Name</th><th>Type</th><th>Purpose</th></tr>
  <tr>
    <td>_extension_id</td><td>string</td><td>thing ID of the extension for the plugin command</td>
  </tr>
  <tr>
    <td>_plugin_call_arguments</td><td>array of strings</td><td>arguments to the command supplied by the player in their shell</td>
  </tr>
  <tr>
    <td>_plugin_call_id</td><td>string</td><td>Unique identifier for this call to the plugin</td>
  </tr>
</table>

Internally, Grounds keeps track of context about every call to a plugin, like who the executing actor and player are. When a plugin calls back to the Grounds API, it has to supply its plugin call ID so that Grounds can retrieve that context and satisfy the API call.

When a listener attribute responds to an event, it actually runs a plugin command. It's worth noting here that the calling actor for the plugin is a built-in "internal" actor with no roles, and the calling player is the extension where the listener attribute resides.

Any arguments which a player supplies in their shell when executing a plugin command are passed along. Special arguments like "me" and "here" are resolved before being supplied to the plugin command.

## Permission to Execute a Plugin Command

The player executing a plugin command must have USE permission for the extension where the command resides. This check can be used to define commands that are only accessible to particular player roles.

Plugins themselves are free to implement their own additional permission checks, such as requiring particular player locations, a certain time of day, and so on.

## Plugin JSON RPC Response

When a plugin is finished, it must write a JSON RPC response on its standard output. The response is consumed by Grounds. In all cases, the response ID must match the request ID.

If the plugin's execution is considered successful, then the response must provide a string result and no error. It's fine for a plugin to return an empty string. If the string is non-empty, it is emitted to the calling player.

If the plugin's execution is considered a failure, then the response must provide an error and no result. Any standard JSON RPC error code is acceptable. Grounds defines the following additional error codes.

<table>
  <tr><th>Value</th><th>Meaning</th></tr>
  <tr><td>-32004</td><td>Not Found</td></tr>
</table>

If an error message is provided, it is emitted to the calling player.

## State

A plugin should use attributes in its extension to store its state. For example, an extension representing a switch to be flipped on and off may use an attribute to represent the current switch position. (This could also be an attribute on a proxy object representing the switch in game.) It may also keep a record of who last flipped the switch in a separate attribute.

The `_extension_id` request param makes it easy to work with extension attributes.

## Calling the Grounds API

While a simple plugin can work just from what it receives in requests, more complex plugins need to gather information about the universe and make changes to it. The [Grounds API](api.md) is a limited interface for doing just that.
