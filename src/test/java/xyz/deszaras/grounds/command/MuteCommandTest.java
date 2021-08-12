package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class MuteCommandTest extends AbstractCommandTest {

  private Player mutee;
  private MuteCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    setPlayerRoles(Role.DENIZEN);
  }

  @Test
  public void testSuccess() throws Exception {
    mutee = newTestPlayer("mutee", Role.DENIZEN);
    command = new MuteCommand(actor, player, mutee);

    command.execute();

    assertEquals(List.of(mutee), player.getMuteList());
  }

  @Test
  public void testSuccessAlreadyMuted() throws Exception {
    Player mutee1 = newTestPlayer("mutee1", Role.DENIZEN);
    Player mutee2 = newTestPlayer("mutee2", Role.DENIZEN);
    List<Thing> muteList = new ArrayList<>();
    muteList.add(mutee1);
    muteList.add(mutee2);
    player.setMuteList(muteList);

    command = new MuteCommand(actor, player, mutee2);

    command.execute();

    assertEquals(List.of(mutee1, mutee2), player.getMuteList());
  }

  @Test
  public void testFailureGOD() throws Exception {
    command = new MuteCommand(actor, player, Player.GOD);

    assertThrows(CommandException.class, () -> command.execute());
  }

  @Test
  public void testMuteList() throws Exception {
    Player mutee1 = newTestPlayer("mutee1", Role.DENIZEN);
    Player mutee2 = newTestPlayer("mutee2", Role.DENIZEN);
    List<Thing> muteList = new ArrayList<>();
    muteList.add(mutee1);
    muteList.add(mutee2);
    player.setMuteList(muteList);

    command = new MuteCommand(actor, player, null);

    String muteListing = command.execute();

    assertEquals("mutee1, mutee2", muteListing);
  }

  @Test
  public void testMuteListEmpty() throws Exception {
    command = new MuteCommand(actor, player, null);

    String muteListing = command.execute();

    assertEquals("Your mute list is empty.", muteListing);
  }
}
