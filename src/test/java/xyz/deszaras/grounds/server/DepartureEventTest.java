package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class DepartureEventTest {

  private Player departer;
  private DepartureEvent event;

  @BeforeEach
  public void setUp() throws Exception {
    departer = new Player("departer");
    event = new DepartureEvent(departer);
  }

  @Test
  public void testEvent() {
    assertEquals(departer, event.getPlayer());
    assertNull(event.getLocation());
    assertNull(event.getPayload());
  }
}
