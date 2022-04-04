# Grounds Events Plugin

This plugin implements a system for scheduling play events in game. (This is different from the Grounds event model used to trigger listener attributes.)

```
$ $event create "Welcome" "Thanks for joining my game" "January 1, 2022 12:00 pm" "Holodeck"
* Created event 'Welcome'
$ $event list
* Events:
NAME                                     TIME
----                                     ----
Welcome                                  Jan 1, 2022 12:00:00 PM
$ $event get "Welcome"
* Event details:
Name:      Welcome
Time:      Jan 1, 2022 12:00:00 PM
Location:  Holodeck
Organizer: Rehtaoh

Thanks for joining my game
$ $event delete Welcome
* Deleted event 'Welcome'
```

## Commands

### $event list

Lists all events in chronological order.

### $event get

Gets an event by its name.

### $event create

Creates a new event. Arguments: event name, description, start time, and location.

### $event delete

Deletes an event by name. Only an event's organizer (creator) can delete it.

## Installation

Run [events.cmd](events.cmd) as root in the ORIGIN of the universe.

```
run plugins/events/events.cmd
```

## Uninstallation

Destroy the `events_system` extension. This destroys all events too.
