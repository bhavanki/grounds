# Server Operations

Single-user mode is great for getting your universe started, but Grounds is meant to be run in multi-user / server mode. You can perform all of the same world-building operations in server mode, all while others are either helping with running the game or participating in it.

## Prepare to Launch

Before opening up the server to other players, you should take care of a few things.

### Root Password

First, change the password for the "root" actor - the default password is "grounds". Even though you can only log in as root from the device running Grounds itself, it's better to have that extra layer of protection.

```
# actor set_password root <new password>
```

### Guest Actor

If you do not want guests to be able to log in (as the "guest" actor), then change the password for that actor as well, or remove the actor completely.

### Player Homes

Every player has a home, a place in the universe where they "live", or more specifically, where they are automatically teleported to when their actor joins the game. The default home for new players is the origin of the universe, but that location is not usually part of the game, so it's better to initialize every player with a different home.

The easiest way to do this is to switch to their player, teleport to the desired home location, and run the command `home here` to set a new home.

When a player's actor logs out, the player is teleported back to the origin as a holding area. When the player comes back, they are teleported to their home.

### Banners

Grounds displays some banners to users logging in. The _welcome banner_ appears before someone logs in, while the _login banner_ appears afterwards. Grounds has a default welcome banner (but no default login banner), but you can create your own as files, and configure _server.properties_ to point to them.

## Curtains Rise

To start in server mode, pass in the file containing the universe to load.

```bash
$ java -jar grounds.jar --properties etc/server.properties \
  --universe allendra.json
```

After the server has started, connect using `ssh`. By default, the server listens on port 4768, but you can configure a different port in _server.properties_. In order to connect as the GOD player, log in as the "root" actor.

```
$ ssh -p 4768 root@localhost
(accept the SSH host key)
     ____                           _
    / ___|_ __ ___  _   _ _ __   __| |___
   | |  _| '__/ _ \| | | | '_ \ / _` / __|
   | |_| | | | (_) | |_| | | | | (_| \__ \
    \____|_|  \___/ \__,_|_| |_|\__,_|___/

      Welcome to Grounds! Please log in.

Guest? Log in with username / password "guest".

Password authentication
Password:

Hello! You are logged in as root
Your IP address is 127.0.0.1
Enjoy your stay.

Permitted players:
  GOD

Auto-selecting initial player GOD
```

After you select your player (or the player is selected for you), you are inserted into the game. When playing as GOD, you get the usual "#" prompt, while other players get a "$" prompt.

At this point, others may log in as their actors, pick their players, and start the world turning.

## Curtains Fall

To stop the server, either interrupt the server process with Control-C, or issue the "shutdown" command as GOD (only GOD can shutdown the server this way). Shutting down ends the server process and all actors are disconnected, including you.

→ [Connecting](connecting.md)
