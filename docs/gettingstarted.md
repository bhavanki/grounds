# Getting Started

Welcome to Grounds. This is a quick introduction that explains how to create a new universe; populate it with places, things, and players; and start exploring your new world.

## Running in Single-User Mode

Start by running the server in single-user mode. In single-user mode, you are the GOD player who has unlimited powers, including the power of universe creation.

The server needs a properties file for some configuration information. An example file is included with the server code, and should work out fine for starting off. Pass the path to it in the command line that runs the server.

```bash
$ java -jar grounds.jar --properties etc/server.properties --single-user
Welcome to Grounds.
This is single-user mode. Use ^D or 'exit' to quit.

#
```

The "#" prompt indicates that you are playing as GOD.

## Universe Creation

"If you wish to make an apple pie from scratch, you must first invent the universe." - Carl Sagan

In single-user mode, the game loads the default VOID universe. You can't do anything in this universe; the first thing to do is to create a new one.

You work with Grounds by issuing commands at a prompt. The command to create a new universe is "init". (Command names are case-insensitive.) Run the init command with a name for your new universe.

```
# init Allendra
Created universe Allendra
Created origin place 7f534dba-ea2e-484e-9fc9-b0dfb6b038a3
Created lost+found place 8e3e89bb-9877-4425-911c-48ad5800aa6d
Created guest home place a0ca8fd2-758b-4228-8fac-fcd95b9d72ce
```

Your new universe has sprung into existence! The init command automatically creates some places for you.

* The "origin" is a starting place for further construction in the universe. You must be somewhere in a universe to build things in it.
* The "lost+found" place is where orphaned things in the universe wind up.
* The "guest home" place is where guest players start off.

The init command handles teleporting you to the origin, so you are now ready to start filling it up.

## A Basic Map

You will need locations for adventures in this new universe, so start building them. The "build" command gives you the power to build anything.

```
# build place "Town Square"
Created 456b38c2-febf-4f21-8778-62d61d1a1dcc
√ # build place "Tavern"
Created 0f90601d-11d1-4d8b-9b94-1e34e3c3f0ad
```

A couple of things you may have noticed.

* Every thing in the universe has a unique identifier. This is helpful for being very specific with commands that refer to things, but most of the time you will be able to refer to things by their name instead.
* Grounds indicates the success or failure of the previous command with a check mark or exclamation point before the prompt character, respectively.

These new places are floating free in the universe at the moment. To move to either of them, you will need to teleport.

```
# teleport "Town Square"
Town Square

Players present:
- GOD [00000000-0000-0001-0000-000000000000]
```

The output of the "teleport" command is the same as what you get from the "look" command at the destination. At the moment, there isn't much to mention here. First, let's add a description to this place.

```
# describe "Town Square" "The center of town is only roughly square-shaped, surrounded by a motley collection of houses in various states of repair, or disrepair. One of the better-maintained structures is the tavern on the eastern side of the cobblestoned surface."
(the description is echoed back)
√ # look
Town Square

The center of town is only roughly square-shaped, surrounded by a motley
collection of houses in various states of repair, or disrepair. One of the
better-maintained structures is the tavern on the eastern side of the
cobblestoned surface.

Players present:
- GOD [00000000-0000-0001-0000-000000000000]
```

Better. However, even though the tavern is in plain sight, you cannot move there yet except by teleporting. To create a path between the two places, build a link between them.

```
# build link tsq2tvn 456b38c2-febf-4f21-8778-62d61d1a1dcc TS 0f90601d-11d1-4d8b-9b94-1e34e3c3f0ad TAV
Created 2f4262e4-825a-41d1-8153-268beccecd23
√ # look
Town Square

The center of town is only roughly square-shaped, surrounded by a motley
collection of houses in various states of repair, or disrepair. One of the
better-maintained structures is the tavern on the eastern side of the
cobblestoned surface.

Players present:
- GOD [00000000-0000-0001-0000-000000000000]

Exits:
- (TAV) Tavern [0f90601d-11d1-4d8b-9b94-1e34e3c3f0ad]
```

The build command requires five arguments for a link:

1. a name for the link (which in fact doesn't appear in-game)
2. the ID of the "source" of the link
3. the abbreviation to use for the exit to the source from the destination
4. the ID of the "destination" of the link
5. the abbreviation to use for the exit to the destination from the source

Links are not directional, so it doesn't matter which end is the source and which is the destination. You cannot have a link between three or more places, but you can have multiple redundant links.

By the way, if you lose track of the IDs for places, use the "index" command to get a listing of everything in a universe, including their IDs.

Now that the link is in place, you can use the "move" command to enter the tavern.

```
# describe Tavern "Warm lighting suffuses the slightly smoky interior of the establishment. Mismatched tables and chairs provide ample accommodation, and a surprisingly clean and tidy bar stretches across the far wall across from the entrance. Stout kegs and shelves bearing bottles of potent liquids stand behind the bar."
(the description is echoed back)
√ # look
Tavern

Warm lighting suffuses the slightly smoky interior of the establishment.
Mismatched tables and chairs provide ample accommodation, and a surprisingly
clean and tidy bar stretches across the far wall across from the entrance.
Stout kegs and shelves bearing bottles of potent liquids stand behind the bar.

Players present:
- GOD [00000000-0000-0001-0000-000000000000]

Exits:
- (TS) Town Square [456b38c2-febf-4f21-8778-62d61d1a1dcc]
```

You can continue creating places and linking them to construct your complete setting. For now, though, let's create a couple of props to put on the stage.

## Things

Of all possessions a friend is the most precious. - Herodotus

Everything in a universe is, in fact, considered a "thing". There are some special things, like places and links. However, it's possible to build ordinary things that represent, for example, items.

```
# build thing key
Created f2b11fe8-c925-45aa-89ea-1dd4d544d182
√ # build thing tankard
Created f182fcc1-0994-4834-b8ab-c81cb5d1d6e0
√ # look
Tavern

Warm lighting suffuses the slightly smoky interior of the establishment.
Mismatched tables and chairs provide ample accommodation, and a surprisingly
clean and tidy bar stretches across the far wall across from the entrance.
Stout kegs and shelves bearing bottles of potent liquids stand behind the bar.

Players present:
- GOD [00000000-0000-0001-0000-000000000000]

Contents:
- key [f2b11fe8-c925-45aa-89ea-1dd4d544d182]
- tankard [f182fcc1-0994-4834-b8ab-c81cb5d1d6e0]

Exits:
- (TS) Town Square [456b38c2-febf-4f21-8778-62d61d1a1dcc]
```

The new things you built are located in the tavern and listed when you look around. These things can be taken and dropped by players, among other actions.

## Players

Now that the universe has some mildly interesting contents, it's time to define some players. A player is a thing that represents a character in the stories you create.

```
# build player Ahalish DENIZEN
Created a14950c2-44a5-4933-bc5a-49538cd5f89b
√ # build player Rehtaoh DENIZEN
Created 9107e827-f890-4b42-afa0-60aec7359577
√ # look
Tavern

Warm lighting suffuses the slightly smoky interior of the establishment.
Mismatched tables and chairs provide ample accommodation, and a surprisingly
clean and tidy bar stretches across the far wall across from the entrance.
Stout kegs and shelves bearing bottles of potent liquids stand behind the bar.

Players present:
- Ahalish [a14950c2-44a5-4933-bc5a-49538cd5f89b]
- GOD [00000000-0000-0001-0000-000000000000]
- Rehtaoh [9107e827-f890-4b42-afa0-60aec7359577]

Contents:
- key [f2b11fe8-c925-45aa-89ea-1dd4d544d182]
- tankard [f182fcc1-0994-4834-b8ab-c81cb5d1d6e0]

Exits:
- (TS) Town Square [456b38c2-febf-4f21-8778-62d61d1a1dcc]
```

When you build a player, you must define their role in the game. There are a handful of roles; "DENIZEN" is used for an ordinary player with no special permissions, and is appropriate for player characters (PCs).

Your new players are vessels waiting to be occupied by the people who will play your game. For now, though, let's leave them be, and save your work.

## Save the Universe

The "save" command saves the state of the universe. Save the universe, and then exit Grounds.

```
# save allendra.json
Saved universe to allendra.json
√ # exit

Bye!
```

The next time you start the game in single-user mode, you can pick up where you left off by loading the universe.

```
# load allendra.json
Loaded universe from allendra.json
```

Congratulations! You have created a new universe and made some room for adventures and stories to unfold. The next step is to enable you and your friends to access Grounds as the players so you can get to having fun with them.

→ [Managing Actors and Players](actor.md)
