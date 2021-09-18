# Becoming a New Player

## Initial Login

When you join a Grounds server, you are set up with a username and password. This identity represents you as a person - in Grounds parlance, an "actor" - and is not the same as your player in the game universe. For example, you may get the username "velma", with some password, to log in to Grounds, but you will play as a new player named "Rehtaoh".

Often, when you first join a server, you will have only one player assigned to you. In that case, after you log in, Grounds will automatically associate you with your single player so you can get started quickly.

```
$ ssh -p 4768 velma@grounds.example.com
     ____                           _
    / ___|_ __ ___  _   _ _ __   __| |___
   | |  _| '__/ _ \| | | | '_ \ / _` / __|
   | |_| | | | (_) | |_| | | | | (_| \__ \
    \____|_|  \___/ \__,_|_| |_|\__,_|___/

      Welcome to Grounds! Please log in.

Guest? Log in with username / password "guest".

Password authentication
Password:

Hello! You are logged in as velma
Your IP address is 203.0.113.0
Enjoy your stay.

Permitted players:
  1. Rehtaoh

Auto-selecting initial player Rehtaoh
Town Square
...
```

Later on, if or when you decide to participate as other players, Grounds will ask you which player you wish to start with. You can switch to a different player later using the `switch_player` command.

Grounds does permit multiple actors to play as the same player. However, only one actor may inhabit a player at a time.

## Changing Your Password

Your initial password was set by a wizard for you. You should change it to a new one that only you know. Do that using the `change_password` command.

```
$ change_password original new
```

The command takes a moment to complete, since Grounds uses a strong password hashing algorithm which takes time to work.

If you forget your password, a wizard can reset it for you.

## Playing

See [Common Commands](common_commands.md) for the most useful commands to use when playing a game.
