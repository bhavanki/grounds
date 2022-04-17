# Being a Guest

If you are interesting in checking out a Grounds server, you can log in as a guest. A guest has limited capabilities, but can still interact with other players to get to know them and learn about the game.

To log in to Grounds as a guest, connect as the "guest" user. Depending on the server, you may need to connect using `ssh`, or you maybe be able to use `telnet` or a typical MU* client instead.

```
$ ssh -p 4768 guest@grounds.example.com
     ____                           _
    / ___|_ __ ___  _   _ _ __   __| |___
   | |  _| '__/ _ \| | | | '_ \ / _` / __|
   | |_| | | | (_) | |_| | | | | (_| \__ \
    \____|_|  \___/ \__,_|_| |_|\__,_|___/

      Welcome to Grounds! Please log in.

Guest? Log in with username / password "guest".

Password authentication
Password:

Hello! You are logged in as guest
Your IP address is 203.0.113.0
Enjoy your stay.

GUEST LOUNGE

Welcome, guests! This is the guest lounge, where you may talk with players and
other guests, and perhaps do a little exploring. Try these commands to get
started.

- `look` to see where you are and who is also here
- `say Hi` to say "Hi" to everyone here
- `help commands` to learn about other available commands
- `exit` to log out of the server

Players present:
- guest1

```

As a guest, you are automatically placed into the guest lounge, which is the common home for all guests connected to the server. Your temporary player receives a unique name, so there may be multiple guests lingering. There may also be wizards present who you can chat with.

Normally, you can't explore anywhere from the guest lounge, although wizards are free to set up some area of the game for you to roam in, if they wish.

When you are ready to go, use the `exit` command to leave. If you log back in, you might get a different temporary player name.

See [Common Commands](common_commands.md) for some of the commands you can use while you are visiting. Not all of them may be available to you, depending on how the wizards have set up the guest experience.

→ [Becoming a New Player](player.md)
