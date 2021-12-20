# Events

Commands are what makes things happen in a Grounds universe, and many commands generate events describing what happens. Events on their own are invisible to players, but [listener attributes on extensions](extensions.md) can react to events by running their own scripts, which may in turn run more commands. This helps to make a universe feel more alive, beyond the players in it.

## Event Types

Every event has a type which describes what happened to trigger it. These are tied to the command that caused the event to be posted in the first place.

<table>
  <tr><th>Type</th><th>Command</th><th>Cause</th></tr>
  <tr>
    <td>DroppedThingEvent</td><td>DROP</td><td>A thing was dropped by a player</td>
  </tr>
  <tr>
    <td>SayMessageEvent</td><td>SAY</td><td>A message was said</td>
  </tr>
  <tr>
    <td>TakenThingEvent</td><td>TAKE</td><td>A thing was taken by a player</td>
  </tr>
  <tr>
    <td>TeleportArrivalEvent</td><td>TELEPORT</td><td>A thing arrived at a location</td>
  </tr>
  <tr>
    <td>TeleportDepartureEvent</td><td>TELEPORT</td><td>A thing departed from a location</td>
  </tr>
  <tr>
    <td>YoinkArrivalEvent</td><td>YOINK</td><td>A yoinked thing arrived at a location</td>
  </tr>
  <tr>
    <td>YoinkDepartureEvent</td><td>YOINK</td><td>A yoinked thing departed from a location</td>
  </tr>
</table>

**Note:** In Grounds, the MOVE command causes a TELEPORT, so ordinary player movement also triggers `Teleport*` events.

## Event Payload

Every event has a JSON object called a _payload_ with context about the event, beyond the type. Each event type may have its own payload structure. The following fields are standard, but they are not necessarily present in every event.

<table>
  <tr><th>Field name</th><th>Definition</th></tr>
  <tr>
    <td>location</td><td>name of location where event occurred</td>
  </tr>
  <tr>
    <td>locationId</td><td>ID of location where event occurred</td>
  </tr>
  <tr>
    <td>player</td><td>name of player who caused event</td>
  </tr>
  <tr>
    <td>playerId</td><td>ID of player who caused event</td>
  </tr>
</table>

Here are the payload fields provided by the different event types.

<table>
  <tr><th>Type</th><th>Player</th><th>Location</th><th>Other</th></tr>
  <tr>
    <td>DroppedThingEvent</td><td>dropping player</td><td>dropping location</td>
    <td>
      <ul>
        <li>thingId = ID of dropped thing</li>
        <li>thingName = name of dropped thing</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>SayMessageEvent</td><td>saying player</td><td>saying location</td>
    <td>
      <ul>
        <li>message = said message</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>TakenThingEvent</td><td>taking player</td><td>taking location</td>
    <td>
      <ul>
        <li>thingId = ID of taken thing</li>
        <li>thingName = name of taken thing</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>TeleportArrivalEvent</td><td>arriving player</td><td>location of arrival</td><td>none</td>
  </tr>
  <tr>
    <td>TeleportDepartureEvent</td><td>departing player</td><td>location of departure</td><td>none</td>
  </tr>
  <tr>
    <td>YoinkArrivalEvent</td><td>not present</td><td>location of arrival</td>
    <td>
      <ul>
        <li>yoinkedThingId = ID of arriving thing</li>
        <li>yoinkedThingName = name of arriving thing</li>
        <li>yoinkedThingType = type (as simple class name) of arriving thing</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>YoinkDepartureEvent</td><td>not present</td><td>location of departure</td>
    <td>
      <ul>
        <li>yoinkedThingId = ID of departing thing</li>
        <li>yoinkedThingName = name of departing thing</li>
        <li>yoinkedThingType = type (as simple class name) of departing thing</li>
      </ul>
    </td>
  </tr>
</table>
