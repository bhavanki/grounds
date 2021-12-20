# Scripts

[Extensions](extensions.md) can hold scripts written in [Groovy](https://groovy-lang.org/) to implement scripted commands and listener attributes. First learn about how extensions work before getting into the reference details here about what scripts can do.

## Elements of a Scripted Command

A _scripted command_ is a Grounds command that is implemented with a script. It has the following elements.

* An actor who is executing the command, just like an ordinary Grounds command.
* A player who is executing the command, also just like an ordinary Grounds command.
* A script to run, and by association a reference to the script's extension.
* Optional string arguments to pass to the script.

When a listener attribute responds to an event, it actually runs a scripted command. Here, though, the actor for the script is a built-in "internal" actor with no roles, and the player is the extension where the listener attribute resides.

Any arguments which a player supplies in their shell when executing a scripted command are passed along. Special arguments like "me" and "here" are resolved before being supplied to the scripted command.

## Permission to Execute a Scripted Command

The player executing a scripted command must have USE permission for the extension where the command resides. This check can be used to define commands that are only accessible to particular player roles.

Scripts themselves are free to implement their own additional permission checks, such as requiring particular player locations, a certain time of day, and so on.

## Bindings

Several pieces of information are bound to the script before execution. This makes them accessible as ordinary variables in the script itself. All bound variables are strings.

* Script arguments are bound to the variables `arg0`, `arg1`, and so on.
* The ID of the extension where the script resides is bound as `extensionId`.

## State

A script should use attributes in its extension to store its state. For example, an extension representing a switch to be flipped on and off may use an attribute to represent the current switch position. (This could also be an attribute on a proxy object representing the switch in game.) It may also keep a record of who last flipped the switch in a separate attribute.

The `extensionId` variable bound to a script when executing makes it easy to work with extension attributes.

## Caller versus Runner

The terms _caller_ and _runner_ refer to the player or players who are associated with script execution.

* The script _caller_ is the thing that originally caused the script to execute. The caller cannot change during execution.
* The script _runner_ is the thing whose roles and permissions apply to the script. Initially, the script runner is the caller, but a script may switch runners during execution.

## Script API

Beyond bound variables, Grounds offers scripts a limited API to gather information about the universe and make changes to it.

**Well-behaved scripts must only use bound variables and the script API to access the game universe.** Using any other means to access in-game state will cause odd game behaviors and could lead to data corruption. Grounds operators have the option to use a Java security manager (see below) to actively restrict script activity.

The API is listed below.

**exec(List<String>)**

Arguments: command line

Return type: `xyz.deszaras.grounds.command.CommandResult`

Executes the given command line as the script runner, as if they had entered it into their own shell. This is the primary means for scripts to work with the game universe.

Under the hood, Grounds executes only one command at a time. Commands run using `exec` are run as part of their encompassing scripted command, so a script does not need to wait for results, and no other commands will interfere. However, a script should not spend too long executing commands, so that the server can respond to commands from other players.

**failure(String)**

Arguments: failure message

Return type: `xyz.deszaras.grounds.command.CommandException`

Creates an exception with the given message. Throw this exception to cause the script to fail.

**getCallerName()**

Return type: `String`

Gets the name of the script caller.

**getCallerTimezone()**

Return type: `java.time.ZoneId`

Gets the timezone of the script caller, if their actor has set one. If the caller has no actor, or the actor has not set a timezone, returns `ZoneOffset.UTC`.

**getAttr(String,String,String)**

Arguments: thing ID, attribute name, not-found message

Return type: `xyz.deszaras.grounds.model.Attr`

Gets an attribute with the given name from the referenced thing. If the thing or attribute is not present, throws an exception with the given message.

**getAttrInAttrList(String,String,String,String)**

Arguments: thing ID, list attribute name, name of attribute in list, not-found message

Return type: `xyz.deszaras.grounds.model.Attr`

Gets an attribute with the given name (third argument) from a list attribute with the given name (second argument) from the referenced thing. If the thing or either attribute is not present, throws an exception with the given message.

**getAttrNames(String,String)**

Arguments: thing ID, not-found message

Return type: `List<String>`

Lists the names of all attributes of the referenced thing. If the thing is not present, throws an exception with the given message.

**hasAttr(String,String)**

Arguments: thing ID, attribute name

Return type: `boolean`

Returns true if the referenced thing has an attribute with the given name.

**logDebug(String)**

Arguments: log message

Logs the given message at DEBUG level on the server.

**logError(String)**

Arguments: log message

Logs the given message at ERROR level on the server.

**newAttr(String,String)**

Arguments: attribute name, attribute value

Return type: `xyz.deszaras.grounds.model.Attr`

Creates a new STRING attribute with the given name and value.

**newAttr(String,java.time.Instant)**

Arguments: attribute name, attribute value

Return type: `xyz.deszaras.grounds.model.Attr`

Creates a new TIMESTAMP attribute with the given name and value.

**newAttr(String,xyz.deszaras.grounds.model.Thing)**

Arguments: attribute name, attribute value

Return type: `xyz.deszaras.grounds.model.Attr`

Creates a new THING attribute with the given name and value. The string value of a THING attribute is the thing's ID.

**newRecordOutput()**

Return type: `xyz.deszaras.grounds.util.RecordOutput`

Creates a new record output to help with output formatting.

**newTabularOutput()**

Return type: `xyz.deszaras.grounds.util.TabularOutput`

Creates a new tabular output to help with output formatting.

**parseJson(String)**

Arguments: JSON string

Return type: `Object`

Parses a string containing JSON into equivalent Groovy data structures. The returned object can be accessed using Groovy's typical operators for working with objects and lists. For more, see the [JsonSlurper documentation](http://docs.groovy-lang.org/latest/html/documentation/index.html#json_jsonslurper).

**removeAttr(String,String)**

Arguments: thing ID, attribute name

Removes an attribute with the given name from the referenced thing.

**runAsExtension()**

Changes the script runner to its extension. This implies that further script activity is subject to the extension's roles.

**sendMessageTo(String,String)**

Arguments: player name, message

Sends the given message from the script runner to the named player.

**sendMessageToCaller(String)**

Arguments: message

Sends the given message from the script runner to the script caller.

**setAttr(String,String,String)**

Arguments: thing ID, attribute name, attribute value

Sets a STRING attribute on the referenced thing.

**setAttr(String,String,List<xyz.deszaras.grounds.model.Attr>)**

Arguments: thing ID, attribute name, attribute value

Sets an ATTRLIST attribute on the referenced thing.

**setAttrInAttrListValue(String,String,String,String)**

Arguments: thing ID, list attribute name, name of attribute in list, attribute value

Sets an attribute with the given name (third argument) in a list attribute with the given name (second argument) in the referenced thing. If the attribute value is null, the attribute is removed from the list.

## Exceptions

A script should throw an exception when it fails. The best way to do this is to throw an `xyz.deszaras.grounds.command.CommandException` with an appropriate error message. However, any exception thrown by a script is caught and logged by Grounds, but as an unexpected problem.

Use the `failure` function to easily construct an exception to throw.

```
throw failure("It's after midnight")
```

## Script Security

A script runs within the same Java runtime as the rest of the Grounds server, which opens up many security concerns. For example, a script could shutdown the server easily by calling `System.exit`. It is also not difficult for a script to get unfettered access to the current universe by calling static methods built into Grounds.

To address these concerns, Grounds can be run with a security manager. When running under a security manager, key functions are guarded by permissions which the current protection domain (principals / identities and code) must possess in order for them to work. Without a security manager, script activity is not guarded, so malignant scripts may cause problems.

**Important note: The Java security manager is being deprecated due to [JEP 411](https://openjdk.java.net/jeps/411). The documentation here therefore only covers the basics, in anticipation of needing to replace the security manager with a different mechanism.**

To run Grounds with a security manager active, run the Java interpreter running Grounds with the following options.

* `-Djava.security.manager=xyz.deszaras.grounds.server.GroundsSecurityManager`
* `-Djava.security.policy=/path/to/java.policy`

`GroundsSecurityManager` is a typical security manager, except that it blocks the use of `System.exit`.

An example policy file to use is in the _etc_ directory of the Grounds source. It grants all permissions to Grounds code, but grants only very specific permissions to code originating from Groovy scripts, e.g., scripted commands.
