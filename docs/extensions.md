# Extensions

Extensions are ways to add new features to a Grounds universe. You can add behaviors to an extension by referring to plugins in its attributes. Extensions can react to events occurring in the universe, and can offer new functionality through plugin commands.

Let's learn about how extensions work using a couple of examples.

## Plugin Commands

An extension can offer one or more _plugin commands_ for use by players. The name of a plugin command starts with a dollar sign `$` to distinguish it from built-in Grounds commands. Suppose that we want to introduce a command to flip a coin. Using it will look like this:

```
$ $coinflip
```

### Building an Extension

The first step is to create an extension where the plugin command will live. Only some wizard roles (and GOD) are allowed to create extensions.

```
# build extension random
Created 5bbde998-59e8-4513-abfd-b9a20633c567
```

Extensions are things in the universe, just like players and other objects; however, an extension that only offers plugin commands usually does not have a location. When a player tries to execute a plugin command, Grounds looks through all extensions in the universe for one that has an attribute that matches the command name. So, for a "$coinflip" command, an extension needs an attribute named "$coinflip".

The type of the attribute must be ATTRLIST, and Grounds looks for specific attributes to be present in that list.

### Plugin Path and Plugin Method

The only required attributes in the attribute list for a plugin command are "pluginPath" and "pluginMethod".

* The value of the pluginPath attribute is a string containing the path to a plugin for executing the command.
* The value of the pluginMethod attribute is a string containing the RPC method for executing the command.

A plugin is an executable separate from Grounds that performs whatever computations and actions a plugin command needs. You can write a plugin in any language you like!

Grounds talks to plugins using [JSON RPC 2.0](https://www.jsonrpc.org/specification). So, the pluginMethod attribute for a plugin command is the JSON RPC method that Grounds uses in the request that it sends to the plugin.

Grounds ships with a ["random" plugin](../plugins/random/plugin_random.sh) that implements the "$coinflip" command (and other commands) in bash. Here's an overview of how it works.

* The script receives a JSON RPC request from Grounds on standard input. It uses `jq` to extract interesting information from the request: the method, the request ID, and the _plugin call ID_.
* Plugins can call back to the Grounds API to interact with the universe. This script calls `getCallerName` to find the name of the player who is executing the plugin command.
* Plugin commands cannot interact with the game universe directly, but they may execute their own commands, through the API, to do so. Here, the script calls `exec` to execute the POSE command, informing the calling player of the result of the coin flip.

A plugin command may return a string message to be emitted to the calling player. In this example, since the POSE command handles that, the command returns nothing on success.

See [Writing Plugins](plugins.md) for much more information on how to write a plugin.

### JSON or YAML Representation

Since a plugin command is an attribute, it is convenient to maintain it in a JSON or YAML file that can be loaded using the SET_ATTR command. Here is the entire "$coinflip" command as YAML; this is the value of the "$coinflip" attribute of the "random" extension created at the beginning.

```
- name: pluginPath
  type: STRING
  value: plugins/random/plugin_random.sh
- name: pluginMethod
  type: STRING
  value: coinflip
- name: commandHelp
  type: ATTRLIST
  value: |
    - name: $COINFLIP
      type: ATTRLIST
      value: |
        - name: syntax
          value: $COINFLIP
        - name: summary
          value: Flips a coin
        - name: description
          value: |
            The outcome of the flip is posed by the command in the caller's name.
```

To add this to the extension:

```
# set_attr random $coinflip[ATTRLIST]=@coinflip.yaml
```

### Permissions

In order for a player to execute a plugin command, they must have the USE permission for the command's extension. By default, newly created things, including extensions, grant the USE permission to all player roles except guests. You can alter command permissions with the CHANGE_POLICY command. For example, it's safe for guests to flip coins, so:

```
# change_policy random u+g
```

An extension itself is implemented as a player, so it may have its own roles. This is helpful if a plugin command should be allowed to perform an action that the player executing it is not allowed to do according to their own roles; that is, an extension role is a form of privilege escalation. To grant a role to an extension, use the `role add` command. (The random extension does not actually require any additional roles; this is merely an example.)

```
# role add adept random
```

Be sure to trust a plugin command before granting it roles.

### Loading Plugins with the RUN Command

The RUN command executes a series of commands in a batch. It is a convenient mechanism for loading extensions and plugin commands (among other uses). The RUN command takes in a file with the commands to run and feeds them to your shell in order. Comments are supported, and empty lines are ignored.

```
# Adds a $coinflip plugin command.
#
# Run as GOD in the ORIGIN of a universe. Adjust the paths to YAML files as
# necessary for them to load.

build extension random
change_policy random w-dBA
set_attr random $coinflip[ATTRLIST]=@coinflip.yaml

say $coinflip installed.
```

The RUN command may only be executed by GOD.

```
# run etc/ext/random/random.cmd
Running command:
build extension random
Created 61f79128-32fc-414a-895f-8b71812a96a4
Running command:
change_policy random w-dBA
GENERAL: [THAUMATURGE, OWNER, BARD, ADEPT, DENIZEN]
READ: [THAUMATURGE, OWNER, BARD, ADEPT, DENIZEN]
WRITE: [THAUMATURGE, OWNER]
USE: [THAUMATURGE, OWNER, BARD, ADEPT, DENIZEN]
Running command:
set_attr random $coinflip[ATTRLIST]=@coinflip.yaml
Running command:
say $coinflip installed.
> GOD says: $coinflip installed.
```

## Listener Attributes

An extension can have attributes that define responses to events that occur in the game universe. These are called _listener attributes_, because they listen for events and handle them. The name of a listener attribute starts with a caret `^` to clearly distinguish it from other attributes.

Let's use a listener attribute to create a magic fiddle in the game that plays a tune whenever a player enters the fiddle's location. The result will look like this when a player shows up.

```
: The magic fiddle plays a ditty as Ahalish arrives.
Ahalish arrives.
```

### Building an Extension at a Location

An extension with listener attributes may need to be placed at a particular location that is relevant to what it does. In this example, we want the fiddle to only play when a player enters a specific location, so the extension should live there.

```
# build extension magic_fiddle_ext
Created 03bba33e-350b-45e6-9b56-8c1f14cf166b
# yoink magic_fiddle_ext here
magic_fiddle_ext arrives.
```

An extension starts out without a location, so the YOINK command can be used to forcibly relocate it to the desired location.

Next, a listener attribute must be defined. The name of the attribute is not visible to players; here, we'll use "^welcome". The type of a listener attribute must be ATTRLIST, and Grounds looks for specific attributes to be present in that list.

### Plugin Path and Plugin Method

The only required attributes in the attribute list for a listener attribute are "pluginPath" and "pluginMethod".

* The value of the pluginPath attribute is a string containing the path to a plugin for responding to the event.
* The value of the pluginMethod attribute is a string containing the RPC method for responding to the event.

As with plugin commands, you can write plugins for listener attributes in any language.

Grounds ships with a ["magicfiddle" plugin](../plugins/magicfiddle/plugin_magicfiddle.sh) that implements the "^welcome" listener attribute in bash. Here's an overview of how it works.

* The JSON RPC request sent by Grounds contains a list of _plugin call arguments_. The first of those arguments contains the _payload_ for the event as a JSON object. Here, the important part of the payload is the player who triggered the event, whose name is in the "player" field of the payload.
* This time, the script poses as the extension housing the magic fiddle listener attribute. This escalates permission for the script to the roles that the magic fiddle extension itself has. See below for why this is necessary for a listener attribute.

While a plugin command runs on behalf of a player who executes it, a plugin for a listener attribute does not normally run on behalf of anyone in particular; so, there is no obvious player whose roles it should use. Instead, the plugin can execute commands and perform other tasks with the roles of its extension (which is represented as a player in Grounds). In this example, it allows the script to pose in the extension's location.

The script returns nothing. Unlike plugin commands, there is no particular player to respond to.

See [Writing Plugins](plugins.md) for much more information on how to write a plugin.

### Event Type

The optional "eventType" attribute in the attribute list for a listener attribute names the type of event that the extension should respond to, by running the plugin. See [Events](events.md) for a list of available event types.

For the magic fiddle, the event type of interest is "TeleportArrivalEvent", which is fired whenever something arrives at a location.

If a listener has no event type, then it can respond to any type of event.

### Localized

The optional "localized" attribute in the attribute list for a listener attribute indicates whether the extension should respond to events that occur only in its location (true) or from anywhere (false). An extension without any location cannot respond to only local events.

Since the magic fiddle should only play when something arrives at its own location, its localized attribute should be true. This is also why the extension has a location.

If a listener has no localized attribute, then it can respond for events that occur anywhere.

### JSON or YAML Representation

Since a listener attribute is an attribute, it is convenient to maintain it in a JSON or YAML file that can be loaded using the SET_ATTR command. Here is the entire "^welcome" listener attribute as YAML; this is the value of the "^welcome" attribute of the "magic_fiddle_ext" extension created at the beginning.

```
- name: pluginPath
  type: STRING
  value: plugins/magicfiddle/plugin_magicfiddle.sh
- name: pluginMethod
  type: STRING
  value: _listen
- name: eventType
  type: STRING
  value: TeleportArrivalEvent
- name: localized
  type: BOOLEAN
  value: true
```

To add this to the extension:

```
# set_attr magic_fiddle_ext ^welcome[ATTRLIST]=@magicfiddle.yaml
```

### Permissions

Players do not invoke plugins in listener attributes directly, so there is no need to set up policies on extensions containing them.

As described above, an extension should be granted roles that are necessary for its listener attributes to work. To grant a role to an extension, use the `role add` command.

```
# role add bard magic_fiddle_ext
```

Be sure to trust a listener attribute before granting it roles.

### Optional: A Proxy Thing

The magic fiddle extension and listener attribute developed so far work fine, but it may be odd for players to see their effects without there being a magic fiddle visible in the extension's location. Extensions are always invisible to non-wizard players, so instead, let's create an ordinary magic fiddle to go along with it.

```
# build thing "magic fiddle"
# change_policy "magic fiddle" g-d
```

The CHANGE_POLICY command prevents non-wizard players from taking the magic fiddle into their possession.

This pattern is completely optional. In other situations, it may not make sense to represent the effects of a listener attribute visibly.

### Loading Listener Attributes with the RUN Command

As with plugin commands, the RUN command is a convenient mechanism for loading extensions and listener attributes.

```
# Creates an extension representing a magic fiddle that greets arriving players.
# This includes:
# - the magic_fiddle_ext extension
# - the ^welcome listener attribute
# - the proxy magic fiddle thing

# Run as GOD where you want the magic fiddle to live. Adjust the paths to YAML
# files as necessary for them to load.

build extension magic_fiddle_ext
yoink magic_fiddle_ext here
set_attr magic_fiddle_ext ^welcome[ATTRLIST]=@plugins/magicfiddle/magicfiddle.yaml
role add bard magic_fiddle_ext

build thing "magic fiddle"
# stop anyone from picking it up
change_policy "magic fiddle" g-d
describe "magic fiddle" "This faintly sparkling, faintly golden musical instrument hovers steadily in the corner, its bow trained across its strings."

say Magic fiddle installed.
```
