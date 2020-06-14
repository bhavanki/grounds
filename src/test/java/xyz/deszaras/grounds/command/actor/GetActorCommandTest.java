package xyz.deszaras.grounds.command.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

public class GetActorCommandTest extends AbstractCommandTest {

  private static final String USERNAME = "GetActorCommandTest.actor1";

  private GetActorCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    ActorDatabase.INSTANCE.createActorRecord(USERNAME, "password");
  }

  @AfterEach
  public void tearDown() {
    ActorDatabase.INSTANCE.removeActorRecord(USERNAME);
  }

  @Test
  public void testSuccess() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, USERNAME);

    String record = command.execute();
    assertEquals(ActorDatabase.INSTANCE.getActorRecord(USERNAME).get().toString(),
                 record);
  }

  @Test
  public void testFailureNotGod() throws Exception {
    command = new GetActorCommand(actor, player, USERNAME);
    setPlayerRoles(Role.THAUMATURGE);

    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("Only GOD may work with actors", e.getMessage());
  }

  @Test
  public void testFailureForRootActor() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, Actor.ROOT.getUsername());

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("Sorry, you may not work with the root actor", e.getMessage());
  }

  @Test
  public void testFailureMissingActor() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, "nobody");

    CommandException e = assertThrows(CommandException.class,
                                      () -> command.execute());
    assertEquals("I could not find the actor named nobody", e.getMessage());
  }
}
