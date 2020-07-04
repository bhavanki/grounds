# Server Operations

Single-user mode is great for getting your universe started, but Grounds is meant to be run in multi-user / server mode. You can perform all of the same world-building operations in server mode, all while others are either helping with running the game or participating in it.

## Prepare to Launch

Before opening up the server to other players, you should take care of a few things.

First, change the password for the "root" actor - the default password is "grounds". Even though you can only log in as root from the device running Grounds itself, it's better to have that extra layer of protection.

```
# actor set_password root <new password>
```

Also, if you do not want guests to be able to log in (as the "guest" actor), then change the password for that actor as well, or remove the actor completely.

Grounds also displays some banners to users logging in. The _welcome banner_ appears before someone logs in, while the _login banner_ appears afterwards. Grounds has a default welcome banner (but no default login banner), but you can create your own as files, and configure _server.properties_ to point to them.

## Curtains Rise

To start in server mode, pass in the file containing the universe to load.

```bash
$ java -jar grounds.jar --properties etc/server.properties \
  --universe allendra.json
```

After the server has started, connect using `ssh`. By default, the server listens on port 4768, but you can configure a different port in _server.properties_. In order to connect as the GOD player, log in as the "root" actor.

```
$ ssh -p 4768 root@localhost
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

## Roles and Wizards

Each player in the game has one or more roles. A role helps to control what a player is allowed to do. Here are the roles available in the game, and their purposes.

The _DENIZEN_ role is for ordinary player characters. Anyone not on the server staff who has been accepted into the game has this role. A DENIZEN has the ability to freely move around the universe and interact with it, but limited ability to make changes to it.

The _BARD_ role is a wizard role focused on building the world. A BARD may do anything a DENIZEN can, but also has the ability to build and destroy things, and may circumvent some of the rules of the world in order to arrange it as needed for the story.

The _ADEPT_ role is a wizard role for enforcing the rules of the game. An ADEPT cannot build or destroy things, but they have the ability to boot or lock actors and to police the game through extraordinary actions, such as taking things from other players.

The _THAUMATURGE_ role combines the abilities of the BARD and the ADEPT, and more. There are very few actions a THAUMATURGE may not take: loading and saving the game and shutting down the server are some of them. Notably, a THAUMATURGE can fully administer actor accounts except removing them, and can change the roles of other players.

 The GOD player has no roles, but it may do anything. You should rarely play as GOD, but instead take on a player with more limited power. The THAUMATURGE role is appropriate for the core administrators of the server to hold. Staff who are focused on world-building can get by with the BARD role, while other staff who contribute to the smooth running of the game should play as ADEPTs.

## Curtains Fall

To stop the server, either interrupt the server process with Control-C, or issue the "shutdown" command as GOD (only GOD can shutdown the server this way). Shutting down ends the server process and all actors are disconnected, including you.
