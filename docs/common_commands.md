# Common Commands

First, `help commands` provides a complete list of commands that are available, and `help <command>` provides help for a specific command. Grounds offers a lot of commands, so here are the most common and useful ones for players.

## Communication

Grounds is oriented towards roleplaying (RP), and that means players talking and doing things.

### Verbal Communication

To say something out loud, use the `say` command. Everyone in your location has the opportunity to "hear" what you say.

```
$ say Hello there
> Ahalish says: Hello there
```

All players in the same location as the saying player receive the same message with what the saying player says. So, utterances that are "said" should be considered part of the official RP record.

To say something to only one player, use the `whisper` command. Only the targeted player "hears" what is whispered. So, if Ahalish whispers to Rehtaoh:

```
$ whisper Rehtaoh I like you
```

Then Rehtaoh, and only Rehtaoh, sees:

```
~ Ahalish whispers: I like you
```

Whispers aren't often considered part of the official RP record, since only the recipient sees the message. However, in a two-person session, you might want to include whispers.

The command `tell` is an alias for `whisper`, so use either one for one-to-one private local communication.

Not all messages are meant to be part of the RP record or, in other words, meant to be in-character (IC). If you want to say something to the other people playing the game with you without intending for it to be part of the RP record, use the `ooc` command. It works just like `say`, but annotates the message differently.

```
$ ooc Having fun so far?
% Ahalish says OOC: Having fun so far?
```

The `ooc` command is actually an alias for `say _ooc_`, so use either formulation for out-of-character (OOC) messages.

Finally, you can send a message to a player anywhere in the universe using the `page` command. Paged message are all considered OOC. So, if Ahalish pages:

```
$ page Rehtaoh "I'm over here in the tavern."
```

Then Rehtaoh, and only Rehtaoh, sees:

```
% Ahalish says OOC from afar: I'm over here in the tavern.
```

### Quoting

You may have noticed that the example `page` command above used double quotes for its message. In general, Grounds separates command arguments with spaces, and you can use single or double quotation marks to provide arguments that should contain spaces. All of the basic communication commands above have special handling that automatically crams together all arguments (after a player name for `whisper` or `page`) into one, so you don't have to worry about quoting. But, if you use a special character, like an apostrophe, in your message, you'll need to use quotation marks.

For example, to say "I'm ready", either of these works:

```
$ say "I'm ready"
$ say "I'm" ready
```

## Non-Verbal Communication

Besides saying things, players do things. Grounds allows for interaction with things in the universe, but players are free to simply describe what they do, or think, or anything else in general. To do any of those, use the `pose` command.

```
$ pose The moon peeks through gaps in the heavy clouds above.
: The moon peeks through gaps in the heavy clouds above.
```

As with `say` and `ooc`, all players in the same location as the posing player have the opportunity to see the message.

It is common for a player to pose about what they themselves are doing, so Grounds makes it easy to do that using the `:` transform for `pose`. When you pose with `:`, then your player's name is added to the front of the posed message.

```
$ :flexes her muscles
: Rehtaoh flexes her muscles
```

## Muting

Every player has a mute list. Messages from a player on the mute list are not shown to the muting player. This applies to all of the communication commands described above, as well as other areas of Grounds. So, muting is a powerful and pervasive way to block interactions with other players.

Muting applies only to direct communications from the muted player. You will still see a muted player present in the universe.

A player who you mute does not receive any information from Grounds that you have muted them. For example, if they say something when you are in the same location, you do not see what they said, and they do not see any indicator that you have not heard it. The same applies if they whisper to you.

To mute a player and add them to your mute list, use the `mute` command. To take them off the list, use the `unmute` command. The `mute` command without arguments shows your mute list.

```
$ mute Rehtaoh
Muted Rehtaoh
$ mute
Rehtaoh
$ unmute Rehtaoh
Unmuted Rehtaoh
```

## Orientation and Navigation

Besides communicating with other players, Grounds lets you make your way around the universe (game world).

### Looking

Your player is always in some location in the game, and you can find out where that is with the `look` command.

```
$ look
Tavern

Warm lighting suffuses the slightly smoky interior of the establishment.
Mismatched tables and chairs provide ample accommodation, and a surprisingly
clean and tidy bar stretches across the far wall across from the entrance.
Stout kegs and shelves bearing bottles of potent liquids stand behind the bar.

Players present:
- Ahalish
- Rehtaoh

Contents:
- key
- tankard

Exits:
- (TS) Town Square
```

Looking tells you at least the name of the place where you are and its description (if it has one). It optionally shows the following:

* any players that are present, including your own
* any things that are present, as the "contents" of the location
* any exits to elsewhere in the game

The command `l` is a shortcut for `look`.

### Moving

Most locations in the universe should have at least one exit. To traverse an exit and move to another location, use the `go` command. Use the abbreviated name of the exit to indicate where you want your player to go.

```
$ go ts
Town Square
...
```

The command `g` is a shortcut for `go`.

### IDs

Everything in the universe in Grounds has a unique identifier, or ID, associated with it. As a regular player, you usually don't need to know or care about IDs, and so Grounds does not show them to you. However, they are not secret, and if you wish, you can tell Grounds to emit IDs for some command responses.

To tell Grounds to show IDs to you, set your "showids" preference to true using the `preference` command. The `look` command, for example, will reveal IDs when your "showids" preference is true.

```
$ preference showIds=true
{showIds=true}
$ look
Tavern [0f90601d-11d1-4d8b-9b94-1e34e3c3f0ad]

Warm lighting suffuses the slightly smoky interior of the establishment.
...

Players present:
- Ahalish [a14950c2-44a5-4933-bc5a-49538cd5f89b]
- Rehtaoh [9107e827-f890-4b42-afa0-60aec7359577]

Contents:
- key [f2b11fe8-c925-45aa-89ea-1dd4d544d182]
```

Grounds commands primarily work with IDs, such as those of players or things. When you issue a command referencing a thing in the universe by name, Grounds _resolves_ the names into IDs. For example, the command `whisper Rehtaoh Hello` names the player `Rehtaoh`; Grounds looks for a player with that name in your location to figure out exactly who you are whispering to. The following command does the exact same thing, but is much less convenient.

```
whisper 9107e827-f890-4b42-afa0-60aec7359577 Hello
```

Use of an ID instead of a name does not let you circumvent the rules of the universe. For example, the `whisper` command works only if the identified player is actually in the same location as you; if you try it when that player is not present, Grounds will complain that it doesn't see anything with that ID. Again, IDs are not secret values.

### Teleporting

Moving using the `go` command requires traversing links from location to location. You can transport yourself to a remote location in one step using the `teleport` command. You need to specify the destination by its ID.

```
$ teleport 0f90601d-11d1-4d8b-9b94-1e34e3c3f0ad
Tavern

Warm lighting suffuses the slightly smoky interior of the establishment.
...
```

Teleportation makes it possible to jump between areas of the universe that are not linked together. However, there may be policies set at some destinations that prevent you from teleporting there.

The command `tp` is a shortcut for `teleport`.

### Going Home

Every non-guest player has a home. You can teleport to your home with the `home` command.

```
$ home
```

You may also set your home by passing it as an argument. An easy way to do this is to use the special argument "here", which resolves to the ID of your current location.

```
$ home here
```

### Leaving the Game

When it's time to go, use the `exit` command to disconnect. This terminates your SSH session.

```
$ exit
```

## Combat

See [Combat](combat.md) for a description of the combat system and the commands for it.
