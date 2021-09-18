package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class GetIdCommandTest extends AbstractCommandTest {

  private GetIdCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);
  }

  @Test
  public void testThing() throws Exception {
    Thing thing = newTestThing("thingamabob");
    command = new GetIdCommand(actor, player, "thingamabob", Thing.class);

    assertEquals(thing.getId().toString(), command.execute());
  }

  @Test
  public void testPlayer() throws Exception {
    command = new GetIdCommand(actor, player, player.getName(), Player.class);

    assertEquals(player.getId().toString(), command.execute());
  }

  @Test
  public void testPlace() throws Exception {
    Place place = newTestPlace("bathroom");
    command = new GetIdCommand(actor, player, "bathroom", Place.class);

    assertEquals(place.getId().toString(), command.execute());
  }
}
