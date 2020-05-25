package xyz.deszaras.grounds.command;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class InventoryCommandTest extends AbstractCommandTest {

  private InventoryCommand command;

  @Before
  public void setUp() {
    super.setUp();

    command = new InventoryCommand(actor, player);
  }

  @Test
  public void testSuccessNoContents() throws Exception {
    assertEquals("", command.execute());
  }
}
