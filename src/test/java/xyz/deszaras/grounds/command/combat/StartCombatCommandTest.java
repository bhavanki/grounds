package xyz.deszaras.grounds.command.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static xyz.deszaras.grounds.command.combat.CombatCommandTestUtils.initTestCombat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Place;

@SuppressWarnings("PMD.TooManyStaticImports")
public class StartCombatCommandTest extends AbstractCommandTest {

  private static final List<String> TEAM_NAMES = List.of("heroes", "villains");

  private Place location;
  private Combat combat;
  private StartCombatCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    player.setCurrentActor(actor);

    location = newTestPlace("arena");
  }

  @Test
  public void testSuccess() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.of(player));
    when(combat.passes(Category.WRITE, player)).thenReturn(true);
    when(combat.start(any(List.class))).thenReturn(combat);
    when(combat.getAllCombatants()).thenReturn(Set.of(player));
    when(combat.status()).thenReturn("nifty");

    command = new StartCombatCommand(actor, player, TEAM_NAMES);

    String result = command.execute();
    assertEquals("nifty", result);

    ArgumentCaptor<List> teamNamesCaptor = ArgumentCaptor.forClass(List.class);
    verify(combat).start(teamNamesCaptor.capture());
    assertEquals(TEAM_NAMES, teamNamesCaptor.getValue());

    Message message = player.getNextMessage();
    assertEquals(player, message.getSender());
    assertEquals("Combat has started!", message.getMessage());
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);

    command = new StartCombatCommand(actor, player, TEAM_NAMES);

    assertThrows(PermissionException.class, () -> command.execute());
  }

  @Test
  public void testFailureNoCombat() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);

    command = new StartCombatCommand(actor, player, TEAM_NAMES);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("No combat is present"));
  }

  @Test
  public void testFailureDueToNotOwner() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    player.setLocation(location);
    location.give(player);
    combat = initTestCombat("existing", location, testUniverse);
    when(combat.getOwner()).thenReturn(Optional.empty());
    when(combat.passes(Category.WRITE, player)).thenReturn(false);

    command = new StartCombatCommand(actor, player, TEAM_NAMES);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("You lack"));
  }

  @Test
  public void testFailureNoLocation() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new StartCombatCommand(actor, player, TEAM_NAMES);

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertTrue(e.getMessage().contains("start combat"));
  }
}
