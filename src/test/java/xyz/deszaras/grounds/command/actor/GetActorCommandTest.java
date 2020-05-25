package xyz.deszaras.grounds.command.actor;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.command.PermissionException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

public class GetActorCommandTest extends AbstractCommandTest {

  private static final String USERNAME = "GetActorCommandTest.actor1";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private GetActorCommand command;

  @Before
  public void setUp() {
    super.setUp();

    ActorDatabase.INSTANCE.createActorRecord(USERNAME, "password");
  }

  @After
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

    thrown.expect(PermissionException.class);
    thrown.expectMessage("Only GOD may work with actors");

    command.execute();
  }

  @Test
  public void testFailureForRootActor() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, Actor.ROOT.getUsername());

    thrown.expect(CommandException.class);
    thrown.expectMessage("Sorry, you may not work with the root actor");

    command.execute();
  }

  @Test
  public void testFailureMissingActor() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, "nobody");

    thrown.expect(CommandException.class);
    thrown.expectMessage("I could not find the actor named nobody");

    command.execute();
  }
}
