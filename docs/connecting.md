# Connecting

Historically, MUSH servers have always used the [Telnet](https://en.wikipedia.org/wiki/Telnet) protocol. All popular MU* clients support Telnet, and users may also use the basic `telnet` command to connect.

Grounds intentionally prefers the [Secure Shell](https://en.wikipedia.org/wiki/Secure_Shell) (or SSH) protocol, since it encrypts all communications between Grounds and its users. Unfortunately, MU* clients generally do not support SSH.

Out of the box, Grounds only uses SSH, but it can be configured to use Telnet instead, or even to use both at the same time on different ports. Users just need to be informed which protocol to use on which port.

Obviously, you can't use the same port for both SSH and Telnet. Also, at least one of them must be enabled.

## SSH Connectivity

To enable or disable SSH, set "enableSsh" in _server.properties_ to true (default) or false, respectively. Choose a port with the "sshPort" property, default 4768.

Grounds requires a host key for SSH. The path to the file is set by "hostKeyFile" in _server.properties_. When Grounds starts up with SSH enabled, it generates a new key if the file does not yet exist. Be sure to protect this file, because it can be used to impersonate the server if it's compromised!

SSH does not support unauthenticated access, so non-guest users must already have an [actor](actor.md) established in order to connect at all.

```
$ ssh -p 4768 wilma@grounds.example.com
```

[Guest](guest.md) users can authenticate as the pre-existing "guest" actor. Grounds creates a new, temporary guest player for each session; the player is destroyed when the user disconnects.

### Clients

The standard `ssh` client is good enough for connecting to Grounds over SSH, although the experience isn't the same as a decent MU* client. While Windows now ships with an `ssh` client, [PuTTY](https://www.chiark.greenend.org.uk/~sgtatham/putty/) is probably nicer.

## Telnet Connectivity

To enable or disable Telnet, set "enableTelnet" in _server.properties_ to true or false (default), respectively. Choose a port with the "telnetPort" property, default 4769.

After connecting over Telnet, a user is prompted to either connect or exit.

```
Log in with 'connect <username> <password>'
  or use 'exit' to disconnect

>
```

Non-guest users can authenticate with their actor usernames and passwords.

[Guest](guest.md) users can authenticate as the pre-existing "guest" actor. Grounds creates a new, temporary guest player for each session; the player is destroyed when the user disconnects.

### Clients

There are an abundance of MU* clients that should work with Grounds. Some fine examples:

* [Atlantis](https://riverdark.net/atlantis/)
* [MUSHclient](https://mushclient.com/mushclient/mushclient.htm)
* [Potato](http://www.potatomushclient.com/)
* [TinTin++](https://tintin.mudhalla.net/)

Grounds does not support Telnet protocol options for MUD, such as GCMP, MSDP, MXP, and the like.

→ [Being a Guest](guest.md)
