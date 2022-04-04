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

The chat system also has a listener attribute for when a guest player is yoinked, which happens when they are created. The guest player is automatically added to the "#guest" channel, if it exists.

*This plugin is still in development. Features like channel visibility and joinability are not yet implemented.*

## Commands

### $chat say

Says something in a chat channel. Arguments: channel name, message.

### $chat join

Joins a channel, if permitted. Arguments: channel name.

### $chat leave

Leaves a channel.

### $chat list

Lists all chat channels visible to the player.

### $chat members

Lists members of a chat channel. You must be a member of a channel yourself to list its members. Arguments: channel name.

### $chat mine

Lists all chat channels the player is a member of.

### $chatadmin create

Creates a new chat channel. Arguments: channel name.

### $chatadmin delete

Deletes a chat channel. Arguments: channel name.

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
