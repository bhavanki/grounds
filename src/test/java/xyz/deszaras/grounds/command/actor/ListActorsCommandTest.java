package xyz.deszaras.grounds.command.actor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.command.AbstractCommandTest;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.server.ActorDatabase;

public class ListActorsCommandTest extends AbstractCommandTest {

  private static final String ROOT_USERNAME = Actor.ROOT.getUsername();
  private static final String USERNAME = "ListActorsCommandTest.actor1";

  private ListActorsCommand command;

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
    command = new ListActorsCommand(actor, Player.GOD);

    String listing = command.execute();
    assertTrue(listing.contains(ROOT_USERNAME));
    assertTrue(listing.contains(USERNAME));
  }
}
