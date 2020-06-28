package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  }

  @Test
  public void testSuccess() throws Exception {
    when(player.getMuteList()).thenReturn(new ArrayList<>());

    mutee = newTestPlayer("mutee", Role.DENIZEN);
    command = new MuteCommand(actor, player, mutee);

    command.execute();

    verify(player).setMuteList(List.of(mutee));
  }

  @Test
  public void testSuccessAlreadyMuted() throws Exception {
    Player mutee1 = newTestPlayer("mutee1", Role.DENIZEN);
    Player mutee2 = newTestPlayer("mutee2", Role.DENIZEN);
    List<Thing> muteList = new ArrayList<>();
    muteList.add(mutee1);
    muteList.add(mutee2);
    when(player.getMuteList()).thenReturn(muteList);

    command = new MuteCommand(actor, player, mutee2);

    command.execute();

    verify(player, never()).setMuteList(any(List.class));
  }

  @Test
  public void testFailureGOD() throws Exception {
    when(player.getMuteList()).thenReturn(new ArrayList<>());

    command = new MuteCommand(actor, player, Player.GOD);

    assertThrows(CommandException.class, () -> command.execute());
  }
}
