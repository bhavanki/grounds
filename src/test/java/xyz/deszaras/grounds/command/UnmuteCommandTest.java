package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

public class UnmuteCommandTest extends AbstractCommandTest {

  private Player mutee1;
  private Player mutee2;
  private UnmuteCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);

    mutee1 = newTestPlayer("mutee1", Role.DENIZEN);
    mutee2 = newTestPlayer("mutee2", Role.DENIZEN);
    List<Thing> muteList = new ArrayList<>();
    muteList.add(mutee1);
    muteList.add(mutee2);
    player.setMuteList(muteList);
  }

  @Test
  public void testSuccess() throws Exception {
    command = new UnmuteCommand(actor, player, mutee2);

    command.execute();

    assertEquals(List.of(mutee1), player.getMuteList());
  }

  @Test
  public void testSuccessAlreadyUnmuted() throws Exception {
    Player mutee3 = newTestPlayer("mutee3", Role.DENIZEN);

    command = new UnmuteCommand(actor, player, mutee3);

    command.execute();

    assertEquals(List.of(mutee1, mutee2), player.getMuteList());
  }
}
