# Grounds

A [MUSH](https://en.wikipedia.org/wiki/MUSH).

![Java CI with Maven](https://github.com/bhavanki/grounds/workflows/Java%20CI%20with%20Maven/badge.svg)

## Features

Grounds is under active development, so features are all new and/or still being worked on to a large extent. There are definitely bugs and missing features, and some concepts are still subject to change.

* **Arbitrary attributes.** Every thing has an unlimited set of named, typed attributes. Some are special, like "name" and "description". Make up any others you need.
* **Role-based permissions.** Every thing in a universe has a security policy that permits categories of actions that affect it only by specified roles. For example, a player can't read another thing's attributes unless the player has a role with READ permission. Roles include a few wizardly ones plus ordinary ones.
* **SSH connectivity.** The game server accepts connections over SSH, so that all traffic is encrypted.
* **Command-line editing.** Grounds uses [JLine](https://github.com/jline/jline3) to deliver powerful command line editing features.
* **Extensibility through softcode (scripted) commands.** Extensions in a universe can hold Groovy scripts that may be executed as commands.
* **Reaction to events.** Extensions can also listen for events, such as players arriving or leaving locations, or saying things, and react by running scripts to respond.
* **Chat.** Players can message each other over OOC, named chat channels. *This feature is implemented completely in softcode!*
* **Mail.** Players can communicate over an internal mail system.

## Requirements

* [JDK](https://adoptopenjdk.net/) 11 or higher
* [Apache Maven](https://maven.apache.org) 3.6.0 or higher (building only)

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

You can use a normal SSH client.

```
$ ssh -p 4768 <hostname-or-ip-address>
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
* A combat system
* More fun stuff

## License

[MIT License](LICENSE)

## Other Credits

Default banner logo from www.patorjk.com, Ivrit font
