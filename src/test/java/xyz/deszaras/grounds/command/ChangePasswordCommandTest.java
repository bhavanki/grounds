package xyz.deszaras.grounds.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.server.ActorDatabase;
import xyz.deszaras.grounds.util.Argon2Utils;

@SuppressWarnings("PMD.TooManyStaticImports")
public class ChangePasswordCommandTest extends AbstractCommandTest {

  private Path adbPath;
  private ChangePasswordCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();
    setPlayerRoles(Role.DENIZEN);

    try {
      adbPath = Files.createTempFile("ChangePasswordCommandTest", "adb");
    } catch (IOException e) {
      fail("Failed to create temp file for actor database", e);
    }
    ActorDatabase.INSTANCE.setPath(adbPath);

    ActorDatabase.INSTANCE.createActorRecord(actor.getUsername(),
                                             Argon2Utils.hashPassword("password"));
  }

  @AfterEach
  public void tearDown() {
    ActorDatabase.INSTANCE.removeActorRecord(actor.getUsername());

    try {
      Files.delete(adbPath);
    } catch (IOException e) {
      fail("Failed to delete temp file for actor database", e);
    }
  }

  @Test
  public void testSuccess() throws Exception {
    command = new ChangePasswordCommand(actor, player, "password", "newPassword");

    assertTrue(command.execute());

    String newPasswordHash =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername()).get().getPassword();
    assertTrue(Argon2Utils.verifyPassword(newPasswordHash, "newPassword"));
  }

  @Test
  public void testFailureIncorrectOldPassword() throws Exception {
    command = new ChangePasswordCommand(actor, player, "wrong", "newPassword");

    assertThrows(CommandException.class, () -> command.execute());

    String oldPasswordHash =
        ActorDatabase.INSTANCE.getActorRecord(actor.getUsername()).get().getPassword();
    assertTrue(Argon2Utils.verifyPassword(oldPasswordHash, "password"));
  }
}
