package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

public class ActorDatabaseTest {

  private ActorDatabase db;

  @BeforeEach
  public void setUp() {
    db = new ActorDatabase();
  }

  @Test
  public void testCreateActorRecord() {
    assertTrue(db.createActorRecord("username", "password"));
    ActorRecord r = db.getActorRecord("username").get();
    assertEquals("username", r.getUsername());
    assertEquals("password", r.getPassword());

    assertFalse(db.createActorRecord("username", "notpassword"));
    assertEquals("password", db.getActorRecord("username").get().getPassword());
  }

  @Test
  public void testGetAllActorRecords() {
    assertTrue(db.createActorRecord("username1", "password1"));
    assertTrue(db.createActorRecord("username2", "password2"));

    Set<ActorRecord> records = db.getAllActorRecords();
    assertEquals(2, records.size());
    assertTrue(records.stream().anyMatch(r -> r.getUsername().equals("username1")));
    assertTrue(records.stream().anyMatch(r -> r.getUsername().equals("username2")));
  }

  @Test
  public void testUpdateActorRecord() {
    assertTrue(db.createActorRecord("username1", "password1"));
    assertTrue(db.createActorRecord("username2", "password2"));

    assertTrue(db.updateActorRecord("username2", r -> r.setPassword("password2a")));

    assertEquals("password2a", db.getActorRecord("username2").get().getPassword());

    assertFalse(db.updateActorRecord("username3", r -> r.setPassword("password3a")));
  }

  @Test
  public void testRemoveActorRecord() {
    assertTrue(db.createActorRecord("username1", "password1"));
    assertTrue(db.createActorRecord("username2", "password2"));

    db.removeActorRecord("username1");
    assertTrue(db.getActorRecord("username1").isEmpty());
    assertFalse(db.getActorRecord("username2").isEmpty());

    db.removeActorRecord("missing");
    assertFalse(db.getActorRecord("username2").isEmpty());
  }
}
