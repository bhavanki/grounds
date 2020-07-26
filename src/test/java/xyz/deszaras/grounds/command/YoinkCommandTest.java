package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkArrival;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkArrivalEvent;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkDeparture;
import xyz.deszaras.grounds.command.YoinkCommand.YoinkDepartureEvent;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

@SuppressWarnings("PMD.TooManyStaticImports")
public class YoinkCommandTest extends AbstractCommandTest {

  private Player yoinkedPlayer;
  private Place source;
  private Place destination;
  private YoinkCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.THAUMATURGE);

    yoinkedPlayer = newTestPlayer("yoinkee", Role.DENIZEN);
    source = newTestPlace("source");
    destination = newTestPlace("destination");

    command = new YoinkCommand(actor, player, yoinkedPlayer, destination);
  }

  @Test
  public void testSuccess() throws Exception {
    source.give(yoinkedPlayer);
    yoinkedPlayer.setLocation(source);

    assertTrue(command.execute());

    assertFalse(source.has(yoinkedPlayer));
    assertTrue(destination.has(yoinkedPlayer));
    assertEquals(destination, yoinkedPlayer.getLocation().get());

    YoinkDepartureEvent yoinkDepartureEvent =
        verifyEvent(new YoinkDepartureEvent(yoinkedPlayer, source), command);
    assertEquals(yoinkedPlayer.getName(), ((YoinkDeparture) yoinkDepartureEvent.getPayload()).yoinkedThingName);
    assertEquals(yoinkedPlayer.getId().toString(), ((YoinkDeparture) yoinkDepartureEvent.getPayload()).yoinkedThingId);
    assertEquals(yoinkedPlayer.getClass().getSimpleName(), ((YoinkDeparture) yoinkDepartureEvent.getPayload()).yoinkedThingType);
    YoinkArrivalEvent yoinkArrivalEvent =
        verifyEvent(new YoinkArrivalEvent(yoinkedPlayer, destination), command);
    assertEquals(yoinkedPlayer.getName(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingName);
    assertEquals(yoinkedPlayer.getId().toString(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingId);
    assertEquals(yoinkedPlayer.getClass().getSimpleName(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingType);
  }

  @Test
  public void testSuccessNoSource() throws Exception {
    assertTrue(command.execute());

    assertTrue(destination.has(yoinkedPlayer));
    assertEquals(destination, yoinkedPlayer.getLocation().get());

    YoinkArrivalEvent yoinkArrivalEvent =
        verifyEvent(new YoinkArrivalEvent(yoinkedPlayer, destination), command);
    assertEquals(yoinkedPlayer.getName(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingName);
    assertEquals(yoinkedPlayer.getId().toString(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingId);
    assertEquals(yoinkedPlayer.getClass().getSimpleName(), ((YoinkArrival) yoinkArrivalEvent.getPayload()).yoinkedThingType);
  }
}
