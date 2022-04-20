# Grounds Chat Plugin

This plugin implements a system of chat channels for OOC (or IC) discussions.

```
$ $chat list
* Channels:
#guest
#random
$ $chat say #random "Hello there"
* [#random] Ahalish: Hello there
$ $chat members #random
* Members of #random
Ahalish
Rehtaoh
```

Chat messages are emitted as the chatting player, so anyone who has them muted does not see their messages.

There are two commands in the plugin: `$chat` for ordinary system usage, and `$chatadmin` for maintaining channels. Any player can use the former, but only wizards may use the latter.

The chat system also has listener attributes for when a guest player arrives, which happens when they are created, and when a guest player departs, which happens when they are destroyed. The guest player is automatically added to or removed from the "#guest" channel, respectively, if the channel exists.

*This plugin is still in development.*

## Commands

### $chat say

Says something in a chat channel. The player must be a member of the channel. Arguments: channel name, message.

### $chat join

Joins a channel, if permitted. Arguments: channel name.

### $chat leave

Leaves a channel.

### $chat list

Lists all chat channels visible to the player.

### $chat members

Lists members of a chat channel. The player must be a member of the channel to list its members. Arguments: channel name.

### $chat mine

Lists all chat channels the player is a member of.

### $chatadmin create

Creates a new chat channel. Arguments: channel name.

### $chatadmin delete

Deletes a chat channel. Arguments: channel name.

### $chatadmin inspect

Inspects a chat channel, reporting on all its details. Arguments: channel name.

### $chatadmin set_visibility

Sets visibility constraints on a channel. A channel may be made visible only to specific roles and/or only to specific players. A player may still join an invisible channel. Arguments: channel name, one or more visibility expressions.

Valid visibility expressions:

* roles=X,Y,... (use roles= to clear constraints)
* players=X,Y,... (use players= to clear constraints)

### $chatadmin set_joinability

Sets joinability constraints on a channel. A channel may be made joinable only by specific roles and/or only by specific players. A player may still see an unjoinable channel. Arguments: channel name, one or more joinability expressions.

Valid joinability expressions:

* roles=X,Y,... (use roles= to clear constraints)
* players=X,Y,... (use players= to clear constraints)

### $chatadmin add_member

Adds a player to a chat channel. Arguments: player name, channel name.

### $chatadmin remove_member

Removes a player from a chat channel. Arguments: player name, channel name.

## Installation

Run [chat.cmd](chat.cmd) as root in the ORIGIN of the universe.

```
run plugins/chat/chat.cmd
```

## Uninstallation

Destroy the `chat_system` extension. This destroys all chat channels too.
