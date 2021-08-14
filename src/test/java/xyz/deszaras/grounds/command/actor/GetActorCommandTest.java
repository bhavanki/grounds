package xyz.deszaras.grounds.command.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.CommandException;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;
// import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

public class GetActorCommandTest extends AbstractCommandTest {

  private static final String ROOT_USERNAME = Actor.ROOT.getUsername();
  private static final String USERNAME = "GetActorCommandTest.actor1";

  private GetActorCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    ActorDatabase.INSTANCE.createActorRecord(ROOT_USERNAME, "root_password");
    ActorDatabase.INSTANCE.createActorRecord(USERNAME, "password");
  }

  @AfterEach
  public void tearDown() {
    ActorDatabase.INSTANCE.removeActorRecord(ROOT_USERNAME);
    ActorDatabase.INSTANCE.removeActorRecord(USERNAME);
  }

  @Test
  public void testSuccess() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, USERNAME);

    String details = command.execute();
    // ActorRecord record = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();

    Pattern p1 = Pattern.compile("^.*Username:.*" + USERNAME + "$", Pattern.MULTILINE);
    assertTrue(p1.matcher(details).find());
  }

  @Test
  public void testSuccessForRootActor() throws Exception {
    command = new GetActorCommand(actor, Player.GOD, ROOT_USERNAME);

    String details = command.execute();
    // ActorRecord record = ActorDatabase.INSTANCE.getActorRecord(ROOT_USERNAME).get();

    Pattern p1 = Pattern.compile("^.*Username:.*" + ROOT_USERNAME + "$", Pattern.MULTILINE);
    assertTrue(p1.matcher(details).find());
  }

  @Test
  public void testFailureForRootActor() throws Exception {
    setPlayerRoles(Role.THAUMATURGE);
    command = new GetActorCommand(actor, player, ROOT_USERNAME);

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
