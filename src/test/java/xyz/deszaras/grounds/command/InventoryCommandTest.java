package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InventoryCommandTest extends AbstractCommandTest {

  private InventoryCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    command = new InventoryCommand(actor, player);
  }

  @Test
  public void testSuccessNoContents() throws Exception {
    assertEquals("", command.execute());
  }
}
