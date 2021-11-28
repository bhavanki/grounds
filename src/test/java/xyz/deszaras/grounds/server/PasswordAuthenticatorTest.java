package xyz.deszaras.grounds.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.util.Argon2Utils;

public class PasswordAuthenticatorTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "test";
  private static final String HASH = Argon2Utils.hashPassword(PASSWORD);
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String HOST_ADDRESS = "192.0.2.123";

  private ActorDatabase.ActorRecord actorRecord;
  private ActorDatabase actorDatabase;
  private PasswordAuthenticator a;

  @BeforeEach
  public void setUp() {
    actorDatabase = mock(ActorDatabase.class);
    a = new PasswordAuthenticator(actorDatabase);
  }

  @Test
  public void testSuccess() {
    actorRecord = mock(ActorDatabase.ActorRecord.class);
    when(actorRecord.getLockedUntil()).thenReturn(null);
    when(actorRecord.getPassword()).thenReturn(HASH);
    when(actorDatabase.getActorRecord(USERNAME)).thenReturn(Optional.of(actorRecord));

    assertTrue(a.authenticate(USERNAME, PASSWORD, HOST_ADDRESS));
  }

  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  @Test
  public void testSuccessRoot() {
    actorRecord = mock(ActorDatabase.ActorRecord.class);
    when(actorRecord.getLockedUntil()).thenReturn(null);
    when(actorRecord.getPassword()).thenReturn(HASH);
    when(actorDatabase.getActorRecord("root")).thenReturn(Optional.of(actorRecord));

    assertTrue(a.authenticate("root", PASSWORD, "127.0.0.1"));
  }

  @Test
  public void testFailureUnknownUser() {
    when(actorDatabase.getActorRecord(USERNAME)).thenReturn(Optional.empty());

    assertFalse(a.authenticate(USERNAME, PASSWORD, HOST_ADDRESS));
  }

  @Test
  public void testFailureLocked() {
    actorRecord = mock(ActorDatabase.ActorRecord.class);
    when(actorRecord.getLockedUntil()).thenReturn(Instant.now().plusSeconds(600));
    when(actorRecord.getPassword()).thenReturn(HASH);
    when(actorDatabase.getActorRecord(USERNAME)).thenReturn(Optional.of(actorRecord));

    assertFalse(a.authenticate(USERNAME, PASSWORD, HOST_ADDRESS));
  }

  @Test
  public void testFailureWrongPassword() {
    actorRecord = mock(ActorDatabase.ActorRecord.class);
    when(actorRecord.getLockedUntil()).thenReturn(null);
    when(actorRecord.getPassword()).thenReturn(HASH);
    when(actorDatabase.getActorRecord(USERNAME)).thenReturn(Optional.of(actorRecord));

    assertFalse(a.authenticate(USERNAME, PASSWORD + "xyz", HOST_ADDRESS));
  }

  @Test
  public void testFailureRootNotAtLocalhost() {
    actorRecord = mock(ActorDatabase.ActorRecord.class);
    when(actorRecord.getLockedUntil()).thenReturn(null);
    when(actorRecord.getPassword()).thenReturn(HASH);
    when(actorDatabase.getActorRecord("root")).thenReturn(Optional.of(actorRecord));

    assertFalse(a.authenticate("root", PASSWORD, HOST_ADDRESS));
  }

}
