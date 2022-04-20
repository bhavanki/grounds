package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Player;

public class ArrivalEventTest {

  private Player arriver;
  private ArrivalEvent event;

  @BeforeEach
  public void setUp() throws Exception {
    arriver = new Player("arriver");
    event = new ArrivalEvent(arriver);
  }

  @Test
  public void testEvent() {
    assertEquals(arriver, event.getPlayer());
    assertNull(event.getLocation());
    assertNull(event.getPayload());
  }
}
