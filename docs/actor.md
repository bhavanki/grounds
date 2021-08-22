# Managing Actors and Players

This guide assumes that you have already [gotten started](gettingstarted.md). Here, you will learn about managing actors and players in Grounds.

## Actors vs. Players

An actor is someone who is logged in to Grounds and playing the game; in other words, a real-life person. When you start Grounds in single-user mode, you automatically connect as the actor named "root", which is the only actor with access to the GOD player. When Grounds is running in its normal multi-user mode, you can log in as "root" or, much more often, as some other identity.

A player is a thing in the game universe that represents a character. When you log in to Grounds, you select the player you would like to inhabit, among those you have permissions to. Players are the avatars who interact directly with the game world.

Grounds allows an actor permission to multiple players, and they may even all be used simultaneously. Also, multiple actors may be granted permission to play as the same player, although only one actor may do so at a time.

Players are created in the universe. Actors, on the other hand, exist in a separate actor database, and there are dedicated actor commands for working with them.

## Opening Scene

Creating an actor is easy.

```
# actor add wilma butterfly
```

A new actor named "wilma" with password "butterfly" is saved into the actor database. This person can now log in to Grounds, although they don't have permission to play as any character yet.

This command takes some time to execute, because Grounds uses a very strong hashing algorithm to protect the password. There is no practical way to retrieve the password later, but if it is forgotten, the "actor set_password" command can be used to set a new one.

Let's give this actor someone to play as. You will need the ID of the player; this is because the actor database spans universes, so actors can be associated with players from several universes.

```
# actor add_player wilma a14950c2-44a5-4933-bc5a-49538cd5f89b
```

Now when "wilma" logs in to Grounds, they can play as the player with that ID. When an actor has multiple options for players, the server presents them with their choices. Also, while already logged in, an actor can use the "switch_player" command to flip over to a different (unoccupied) player.

## Actor Relations

While most actors are swell people, there are always a few who cause trouble. Grounds has some tools that may be employed when negotiations fail.

Use the "actor boot" command to kick an actor off of the server. This terminates the actor's sessions for all of the players they are currently occupying. This command is also useful if an actor's session is somehow stuck and they request it be terminated.

A booted actor can immediately log back in. You have the additional option to lock the actor's account using the "actor lock" command. This prevents the actor from logging in until the time you provide. For example, if an actor must be removed from the server for an hour, first lock their account, and then boot them.

```
# actor lock wilma 2020-07-01T20:30
√ # actor boot wilma
Terminated shell for actor wilma playing as Ahalish
```

An alternative to locking an actor is to set their password to something they do not know.

Grounds does not provide a way to ban IP addresses, although it does keep track of them for informational purposes. Instead, use the firewall capabilities of the operating system to block IP addresses or address ranges. On Linux, use the [`iptables`](https://linux.die.net/man/8/iptables) or [`ufw`](https://wiki.ubuntu.com/UncomplicatedFirewall) utilities to manage network access rules.

→ [Server Operations](serverops.md)
