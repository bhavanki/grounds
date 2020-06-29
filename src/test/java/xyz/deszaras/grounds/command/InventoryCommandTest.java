package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;

public class InventoryCommandTest extends AbstractCommandTest {

  private InventoryCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    command = new InventoryCommand(actor, player);
  }

  @Test
  public void testSuccessNoContents() throws Exception {
    assertEquals("", command.execute());
  }
}
