package xyz.deszaras.grounds.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.auth.Policy.Category;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Thing;

@SuppressWarnings("PMD.TooManyStaticImports")
public class DescribeCommandTest extends AbstractCommandTest {

  private Thing thing;
  private DescribeCommand command;

  @BeforeEach
  public void setUp() {
    super.setUp();

    thing = mock(Thing.class);
  }

  @Test
  public void testSuccessGet() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.getDescription()).thenReturn(Optional.of("awesome"));
    when(thing.passes(Category.READ, player)).thenReturn(true);

    command = new DescribeCommand(actor, player, thing, null);
    assertEquals("awesome", command.execute());
  }

  @Test
  public void testSuccessGetNoDescription() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.getDescription()).thenReturn(Optional.empty());
    when(thing.passes(Category.READ, player)).thenReturn(true);

    command = new DescribeCommand(actor, player, thing, null);
    assertEquals("No description available", command.execute());
  }

  @Test
  public void testFailureUndescribable() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.passes(Category.READ, player)).thenReturn(false);

    command = new DescribeCommand(actor, player, thing, null);
    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to see this thing's description",
                 e.getMessage());
  }

  @Test
  public void testSuccessSet() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.passes(Category.WRITE, player)).thenReturn(true);
    when(thing.getDescription()).thenReturn(Optional.of("awesome"));

    command = new DescribeCommand(actor, player, thing, "awesome");
    assertEquals("awesome", command.execute());
    verify(thing).setDescription("awesome");
  }

  @Test
  public void testSuccessRemove() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.passes(Category.WRITE, player)).thenReturn(true);

    command = new DescribeCommand(actor, player, thing,
                                  DescribeCommand.NO_DESCRIPTION);
    assertEquals("No description available", command.execute());
    verify(thing).setDescription(null);
  }

  @Test
  public void testFailureUndescribibable() throws Exception {
    setPlayerRoles(Role.DENIZEN);
    when(thing.passes(Category.WRITE, player)).thenReturn(false);

    command = new DescribeCommand(actor, player, thing, "awesome");
    PermissionException e = assertThrows(PermissionException.class,
                                         () -> command.execute());
    assertEquals("You are not permitted to set this thing's description",
                 e.getMessage());
  }

}
