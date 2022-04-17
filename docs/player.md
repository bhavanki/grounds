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

## Useful Preferences

Your actor has a small set of preferences that customize your interactions with Grounds. These preferences apply to all of your players.

### Timezone

Some Grounds commands, like mail listings, show you timestamps. Your default timezone is UTC, but you can set one as your "tz" preference.

```
$ preference tz=America/New_York
```

A variety of timezone formats are supported. For best results, use a region-based zone ID from the [list of IDs in the IANA timezone database](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones). Some common IDs are:

* America/New_York = US Eastern
* America/Los_Angeles = US Pacific
* Europe/London
* Europe/Berlin
* Asia/Kolkata = India
* Asia/Shanghai = China
* Asia/Tokyo = Japan
* Australia/Sydney

Online tools like [What's My Time Zone?](http://www.timezoneconverter.com/cgi-bin/findzone.tzc) can help you determine the best setting for you.

### Color

Grounds can use [ANSI escape codes](https://en.wikipedia.org/wiki/ANSI_escape_code) to colorize text. By default, these codes are disabled, but you can enable them with your "ansi" preference.

```
$ preference ansi=true
```

Most modern terminal applications support ANSI escape codes, although those in Windows may require some configuration.

While color can improve and enhance the appearance of Grounds, you do not need to enable it to fully access the game; non-color indicators, such as particular symbols, are always provided along with color indicators.

### Prompt Hiding

Normally Grounds presents an input prompt, but this can interfere with how some MU* clients manipulate input to and output from the server. To disable the prompt, use the "hidePrompt" preference.

```
$ preference hidePrompt=true
```

→ [Common Commands](common_commands.md)
