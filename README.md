# Grounds

An extensible [MUSH](https://en.wikipedia.org/wiki/MUSH)/[MUD](https://en.wikipedia.org/wiki/MUD) server written in Java.

![Java CI with Maven](https://github.com/bhavanki/grounds/workflows/Java%20CI%20with%20Maven/badge.svg)

## Features

Grounds is under active development, so features are all new and/or still being worked on to a large extent. There are definitely bugs and missing features.

* **Arbitrary attributes.** Every thing has an unlimited set of named, typed attributes. Some are special, like "name" and "description". Make up any others you need.
* **Role-based permissions.** Every thing in a universe has a security policy that permits categories of actions that affect it only by specified roles. For example, a player can't read another thing's attributes unless the player has a role with READ permission. Roles include a few wizardly ones plus ordinary ones.
* **SSH and telnet connectivity.** The game server accepts connections over SSH, so that all traffic is encrypted, or telnet, or both.
* **Command-line editing.** Grounds uses [JLine](https://github.com/jline/jline3) to deliver powerful command line editing features.
* **Extensibility through plugins.** Extensions in a universe can call on external plugins, written in any language, that may be executed as commands.
* **Reaction to events.** Extensions can also listen for events, such as players arriving or leaving locations, or saying things, and react by running plugins to respond.
* **Chat.** Players can message each other over OOC, named chat channels. *This feature is implemented completely in a plugin!*
* **Events.** Players can maintain a list of upcoming events. *This feature is implemented completely in a plugin!*
* **Mail.** Players can communicate over an internal mail system.
* **Combat.** The Grapple combat system lets players fight for victory against each other or NPCs.

## Requirements

* [JDK](https://adoptopenjdk.net/) 17 or higher
* [Apache Maven](https://maven.apache.org) 3.6.0 or higher (building only)
* [protoc](https://github.com/protocolbuffers/protobuf/) 3.19.3 or higher (building only)

On macOS, Homebrew can install `protoc` via `brew install protobuf`.

## Running

In normal multi-user mode:

```
$ java -jar grounds-<version>.jar \
  --properties etc/server.properties --universe mygame.json
```

In single-user mode:

```
$ java -jar grounds-<version>.jar \
  --properties etc/server.properties --single-user
```

Instructions on building a new universe are in the [documentation](https://bhavanki.github.io/grounds).

## Connecting

You can use a normal SSH or telnet client.

```
$ ssh -p 4768 <username>@<hostname-or-ip-address>
$ telnet -p 4769 <hostname-or-ip-address>
```

After authenticating with your password, you can pick the player you want to use and go have fun.

## Building

```
$ mvn package
```

## Documentation

[https://bhavanki.github.io/grounds](https://bhavanki.github.io/grounds)

## Plans

* Fleshed-out event systems
* More fun stuff

## License

[MIT License](LICENSE)

See [the telnet package README](src/main/java/xyz/deszaras/telnet/README.md) for additional license information pertaining to telnet code in Grounds.

## Other Credits

Default banner logo from www.patorjk.com, Ivrit / Standard font
