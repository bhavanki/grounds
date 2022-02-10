package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.server.ActorDatabase.ActorRecord;

public class PreferenceCommandTest extends AbstractCommandTest {

  private static final String USERNAME = "PreferenceCommandTest.actor1";

  private PreferenceCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    actor = new Actor(USERNAME);

    ActorDatabase.INSTANCE.createActorRecord(USERNAME, "password");
  }

  @AfterEach
  public void tearDown() {
    ActorDatabase.INSTANCE.removeActorRecord(USERNAME);
  }

  @Test
  public void testSetFirst() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    assertTrue(actor.getPreferences().isEmpty());

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    String result = command.execute();

    assertEquals("key1 = value1", result);
    assertEquals(1, actor.getPreferences().size());
    assertEquals("value1", actor.getPreference("key1").get());

    ActorRecord actorRecord = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();
    assertEquals(actor.getPreferences(), actorRecord.getPreferences());
  }

  @Test
  public void testSetAFew() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, "key2=value2", false);
    String result = command.execute();

    assertEquals("key2 = value2", result);
    assertEquals(1, actor.getPreferences().size());
    assertEquals("value2", actor.getPreference("key2").get());

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    result = command.execute();

    assertEquals("key1 = value1, key2 = value2", result);
    assertEquals(2, actor.getPreferences().size());
    assertEquals("value1", actor.getPreference("key1").get());
    assertEquals("value2", actor.getPreference("key2").get());

    command = new PreferenceCommand(actor, player, "key3=value3", false);
    result = command.execute();

    assertEquals("key1 = value1, key2 = value2, key3 = value3", result);
    assertEquals(3, actor.getPreferences().size());
    assertEquals("value1", actor.getPreference("key1").get());
    assertEquals("value2", actor.getPreference("key2").get());
    assertEquals("value3", actor.getPreference("key3").get());

    ActorRecord actorRecord = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();
    assertEquals(actor.getPreferences(), actorRecord.getPreferences());
  }

  @Test
  public void testChange() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    String result = command.execute();

    assertEquals("key1 = value1", result);

    command = new PreferenceCommand(actor, player, "key1=value1a", false);
    result = command.execute();

    assertEquals("key1 = value1a", result);
    assertEquals(1, actor.getPreferences().size());
    assertEquals("value1a", actor.getPreference("key1").get());

    ActorRecord actorRecord = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();
    assertEquals(actor.getPreferences(), actorRecord.getPreferences());
  }

  @Test
  public void testRemove() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    command.execute();
    command = new PreferenceCommand(actor, player, "key2=value2", false);
    String result = command.execute();

    assertEquals("key1 = value1, key2 = value2", result);

    command = new PreferenceCommand(actor, player, "key1=", false);
    result = command.execute();

    assertEquals("key2 = value2", result);
    assertEquals(1, actor.getPreferences().size());
    assertEquals("value2", actor.getPreference("key2").get());

    ActorRecord actorRecord = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();
    assertEquals(actor.getPreferences(), actorRecord.getPreferences());
  }

  @Test
  public void testGet() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, null);
    String result = command.execute();

    assertEquals("", result);

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    command.execute();
    command = new PreferenceCommand(actor, player, "key2=value2", false);
    command.execute();

    command = new PreferenceCommand(actor, player, null);
    result = command.execute();

    assertEquals("key1 = value1, key2 = value2", result);
  }

  @Test
  public void testEqualsInValue() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, "key1=value1=value1a", false);
    String result = command.execute();

    assertEquals("key1 = value1=value1a", result);
    assertEquals(1, actor.getPreferences().size());
    assertEquals("value1=value1a", actor.getPreference("key1").get());

    ActorRecord actorRecord = ActorDatabase.INSTANCE.getActorRecord(USERNAME).get();
    assertEquals(actor.getPreferences(), actorRecord.getPreferences());
  }

  @Test
  public void testFailureInvalidPreferenceString() throws Exception {
    setPlayerRoles(Role.DENIZEN);

    command = new PreferenceCommand(actor, player, "key1");
    CommandException e = assertThrows(CommandException.class, () -> command.execute());
    assertTrue(e.getMessage().contains("Invalid preference string"));

    command = new PreferenceCommand(actor, player, "=value1");
    e = assertThrows(CommandException.class, () -> command.execute());
    assertTrue(e.getMessage().contains("Invalid preference string"));
  }

  @Test
  public void testFailureDueToPermission() throws Exception {
    setPlayerRoles(Role.GUEST);

    command = new PreferenceCommand(actor, player, "key1=value1", false);
    assertThrows(PermissionException.class, () -> command.execute());
  }
}
