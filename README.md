# Grounds

A [MUSH](https://en.wikipedia.org/wiki/MUSH).

![Java CI with Maven](https://github.com/bhavanki/grounds/workflows/Java%20CI%20with%20Maven/badge.svg)

## Features

Grounds is under active development, so features are all new and/or still being worked on to a large extent. There are definitely bugs and missing features, and many concepts are still subject to change.

* **Multiple universes.** Game data is in the form of a multiverse containing one or more named universes. Each universe is its own world full of players, places, and other things.
* **Arbitrary attributes.** Every thing has an unlimited set of named, typed attributes. Some are special, like "name" and "description". Make up any others you need.
* **Role-based permissions.** Every thing in a universe has a security policy that permits categories of actions that affect it only by specified roles. For example, a player can't read another thing's attributes unless the player has a role with READ permission. Roles include a few wizardly ones plus ordinary ones.
* **SSH connectivity.** The game server accepts connections over SSH, so that all traffic is encrypted.
* **Command-line editing.** Grounds uses [JLine](https://github.com/jline/jline3) to deliver powerful command line editing features.

## Requirements

* [JDK](https://adoptopenjdk.net/) 11

## Running

For now, you need to build Grounds yourself. See the Building section below.

In normal multi-user mode:

```
$ java -jar grounds.jar --properties etc/server.properties --multiverse mygame.json
```

In single-user mode:

```
$ java -jar grounds.jar --properties etc/server.properties --single-user
```

Instructions on building a new multiverse will be written in the future.

## Connecting

You can use a normal SSH client.

```
$ ssh -p 4768 <hostname-or-ip-address>
```

After authenticating with your password, you can pick the player you want to use and go have fun.

## Building

Use [Apache Maven](https://maven.apache.org/).

```
$ mvn package
```

## Plans

* OOC messaging and chat system
* Scripted ("softcode") commands - in progress!
* A combat system
* Colors!
* More fun stuff

## License

[MIT License](LICENSE)

## Other Credits

Default banner logo from www.patorjk.com, Ivrit font
